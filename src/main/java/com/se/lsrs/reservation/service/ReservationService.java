package com.se.lsrs.reservation.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.se.lsrs.common.auth.CurrentUserProvider;
import com.se.lsrs.common.error.BusinessException;
import com.se.lsrs.common.error.ErrorCode;
import com.se.lsrs.policy.domain.PolicyKey;
import com.se.lsrs.policy.service.PolicyService;
import com.se.lsrs.reservation.domain.CancelReason;
import com.se.lsrs.reservation.domain.Reservation;
import com.se.lsrs.reservation.domain.ReservationStatus;
import com.se.lsrs.reservation.dto.MyReservationResponse;
import com.se.lsrs.reservation.dto.ReservationResponse;
import com.se.lsrs.reservation.repository.ReservationRepository;
import com.se.lsrs.seat.domain.Seat;
import com.se.lsrs.seat.repository.SeatRepository;
import com.se.lsrs.user.domain.User;
import com.se.lsrs.user.repository.UserRepository;

/**
 * 예약 서비스. 예약 생성(US-06), 내 예약 조회와 학생 직접 취소(US-08)가 있다.
 * 강제 취소 cancelByAdmin()(US-14)은 해당 담당이 추가한다.
 *
 * 취소 가능 조건 (FR-X-01, 설계서 3장): RESERVED 이고 now ≤ 시작 + CHECKIN_WINDOW_MIN.
 * 시작 전 '예약중'과 시작 후 체크인 가능 시간 안의 '체크인 대기'를 포함한다.
 * 체크인 가능 시간이 지난 RESERVED 는 노쇼 처리 대상이므로 취소할 수 없다.
 */
@Service
@Transactional(readOnly = true)
public class ReservationService {

	private static final DateTimeFormatter RESTRICTION_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private final ReservationRepository reservationRepository;

	private final UserRepository userRepository;

	private final SeatRepository seatRepository;

	private final PolicyService policyService;

	private final CurrentUserProvider currentUserProvider;

	private final Clock clock;

	public ReservationService(ReservationRepository reservationRepository, UserRepository userRepository,
			SeatRepository seatRepository, PolicyService policyService, CurrentUserProvider currentUserProvider,
			Clock clock) {
		this.reservationRepository = reservationRepository;
		this.userRepository = userRepository;
		this.seatRepository = seatRepository;
		this.policyService = policyService;
		this.currentUserProvider = currentUserProvider;
		this.clock = clock;
	}

	/**
	 * 예약 생성 (US-06, 설계서 Core Flow 1).
	 *
	 * 순서 (설계서 Core Flow 1 시퀀스): 학생 행 → 좌석 행 비관적 잠금을 먼저 모두 획득한 뒤
	 * 이용 제한 → 정책 → 본인 겹침 → 좌석 겹침을 검사하고 저장한다. 잠금 순서(학생 → 좌석)는 고정이다.
	 * 격리 수준을 READ COMMITTED 로 두어 잠금 뒤의 겹침 조회가 먼저 커밋된 예약을 반드시 읽게 한다
	 * (MySQL 기본 REPEATABLE READ 의 스냅샷 읽기 차단). 같은 좌석·겹치는 시간대 동시 요청은 1건만 생성되고 (AC4),
	 * 예외가 나면 전체가 롤백된다 (AC5).
	 *
	 * @throws BusinessException INVALID_RESERVATION_TIME·RESERVATION_DATE_OUT_OF_POLICY·RESERVATION_DURATION_EXCEEDED
	 * (400), USER_RESTRICTED(403), SEAT_NOT_FOUND(404), DUPLICATE_RESERVATION·SEAT_ALREADY_RESERVED(409)
	 */
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public ReservationResponse reserve(Long seatId, LocalDateTime startAt, LocalDateTime endAt) {
		Long userId = currentUserProvider.currentUserId();
		LocalDateTime now = LocalDateTime.now(clock);

		// 1. 잠금 획득: 학생 → 좌석
		User user = userRepository.findByIdForUpdate(userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHENTICATED));
		Seat seat = seatRepository.findByIdForUpdate(seatId)
			.filter(Seat::isActive)
			.orElseThrow(() -> new BusinessException(ErrorCode.SEAT_NOT_FOUND));

		// 2. 이용 제한 (AC3)
		if (user.isRestricted(now)) {
			throw new BusinessException(ErrorCode.USER_RESTRICTED, "이용 제한 중입니다. %s 이후에 예약할 수 있습니다."
				.formatted(user.getRestrictedUntil().format(RESTRICTION_FORMAT)));
		}

		// 3. 정책: 시간대·예약 가능 기간·최대 이용 시간 (US-07 AC2), 이미 시작된 시간대
		policyService.validateReservation(startAt, endAt);
		if (startAt.isBefore(now)) {
			throw new BusinessException(ErrorCode.INVALID_RESERVATION_TIME, "이미 시작된 시간대는 예약할 수 없습니다.");
		}

		// 4. 겹침: 본인 중복 (AC2, FR-R-11) → 좌석 충돌 (AC4)
		if (reservationRepository.existsActiveOverlapByUser(userId, startAt, endAt, now)) {
			throw new BusinessException(ErrorCode.DUPLICATE_RESERVATION);
		}
		if (reservationRepository.existsActiveOverlapBySeat(seatId, startAt, endAt, now)) {
			throw new BusinessException(ErrorCode.SEAT_ALREADY_RESERVED);
		}

		Reservation saved = reservationRepository.save(Reservation.create(user, seat, startAt, endAt, now));
		return ReservationResponse.from(saved);
	}

	/** 내 예약 이력 (US-08 AC1·2) */
	public List<MyReservationResponse> getMyReservations(ReservationStatus status) {
		Long userId = currentUserProvider.currentUserId();
		LocalDateTime startAtFrom = cancellableStartAtFrom();
		return reservationRepository.findMine(userId, status)
			.stream()
			.map(reservation -> MyReservationResponse.of(reservation, isCancellable(reservation, startAtFrom)))
			.toList();
	}

	/**
	 * 학생 직접 취소 (US-08 AC3~5). 노쇼 카운터는 건드리지 않는다 (FR-X-03).
	 * 좌석 상태는 진행 중 예약으로 계산하므로 취소 즉시 '예약 가능'이 된다 (FR-X-04).
	 *
	 * @throws BusinessException RESERVATION_NOT_FOUND(404, 없거나 다른 학생의 예약),
	 * RESERVATION_NOT_CANCELLABLE(409)
	 */
	@Transactional
	public MyReservationResponse cancelByStudent(Long reservationId, CancelReason reason) {
		Long userId = currentUserProvider.currentUserId();
		Reservation reservation = reservationRepository.findByReservationIdAndUserUserId(reservationId, userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));

		LocalDateTime startAtFrom = cancellableStartAtFrom();
		if (!isCancellable(reservation, startAtFrom)) {
			throw new BusinessException(ErrorCode.RESERVATION_NOT_CANCELLABLE, notCancellableMessage(reservation));
		}
		if (reservationRepository.cancelByStudent(reservationId, reason, startAtFrom) == 0) {
			throw new BusinessException(ErrorCode.RESERVATION_NOT_CANCELLABLE);
		}

		Reservation cancelled = reservationRepository.findById(reservationId).orElseThrow();
		return MyReservationResponse.of(cancelled, false);
	}

	/** 시작 시각이 이 시각 이후인 RESERVED 만 취소할 수 있다 (= now - 체크인 가능 시간) */
	private LocalDateTime cancellableStartAtFrom() {
		return LocalDateTime.now(clock).minusMinutes(policyService.getInt(PolicyKey.CHECKIN_WINDOW_MIN));
	}

	private boolean isCancellable(Reservation reservation, LocalDateTime startAtFrom) {
		return reservation.getStatus() == ReservationStatus.RESERVED
				&& !reservation.getStartAt().isBefore(startAtFrom);
	}

	private String notCancellableMessage(Reservation reservation) {
		return switch (reservation.getStatus()) {
			case CANCELLED -> "이미 취소된 예약입니다.";
			case CHECKED_IN -> "이미 체크인한 예약은 취소할 수 없습니다.";
			case AUTO_CANCELLED -> "노쇼로 자동 취소된 예약입니다.";
			case RESERVED -> "체크인 가능 시간이 지나 취소할 수 없습니다.";
		};
	}

}
