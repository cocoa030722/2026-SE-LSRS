package com.se.lsrs.reservation.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
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
import com.se.lsrs.reservation.repository.ReservationRepository;

/**
 * 예약 서비스. 이 클래스에는 내 예약 조회와 학생 직접 취소(US-08)만 있다.
 * 예약 생성 reserve()(US-06)와 강제 취소 cancelByAdmin()(US-14)은 각 담당이 추가한다.
 *
 * 취소 가능 조건 (FR-X-01, 설계서 3장): RESERVED 이고 now ≤ 시작 + CHECKIN_WINDOW_MIN.
 * 시작 전 '예약중'과 시작 후 체크인 가능 시간 안의 '체크인 대기'를 포함한다.
 * 체크인 가능 시간이 지난 RESERVED 는 노쇼 처리 대상이므로 취소할 수 없다.
 */
@Service
@Transactional(readOnly = true)
public class ReservationService {

	private final ReservationRepository reservationRepository;

	private final PolicyService policyService;

	private final CurrentUserProvider currentUserProvider;

	private final Clock clock;

	public ReservationService(ReservationRepository reservationRepository, PolicyService policyService,
			CurrentUserProvider currentUserProvider, Clock clock) {
		this.reservationRepository = reservationRepository;
		this.policyService = policyService;
		this.currentUserProvider = currentUserProvider;
		this.clock = clock;
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
