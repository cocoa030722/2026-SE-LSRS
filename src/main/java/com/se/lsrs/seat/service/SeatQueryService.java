package com.se.lsrs.seat.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.se.lsrs.common.auth.CurrentUserProvider;
import com.se.lsrs.common.error.BusinessException;
import com.se.lsrs.common.error.ErrorCode;
import com.se.lsrs.policy.service.PolicyService;
import com.se.lsrs.reservation.domain.ReservationStatus;
import com.se.lsrs.reservation.repository.ReservationRepository;
import com.se.lsrs.reservation.repository.SeatOccupancy;
import com.se.lsrs.seat.domain.Seat;
import com.se.lsrs.seat.domain.SeatStatus;
import com.se.lsrs.seat.dto.SeatStatusResponse;
import com.se.lsrs.seat.repository.SeatRepository;

/**
 * 좌석 현황 조회 (US-05). 좌석 상태는 저장하지 않고 조회 시간대와 겹치는 진행 중 예약으로 계산한다 (설계서 3장).
 * 따라서 예약·취소·자동 취소가 커밋되면 다음 조회에 즉시 반영된다 (AC3, FR-X-04·13).
 * 좌석 목록 1회 + 겹치는 예약 1회로 조회해 좌석 수와 무관하게 쿼리 수가 일정하다 (AC5).
 */
@Service
@Transactional(readOnly = true)
public class SeatQueryService {

	/** "HH:mm". 운영 종료 24:00 을 표현하려고 LocalTime 대신 직접 해석한다. */
	private static final Pattern TIME = Pattern.compile("(\\d{1,2}):(\\d{2})");

	private final SeatRepository seatRepository;

	private final ReservationRepository reservationRepository;

	private final PolicyService policyService;

	private final CurrentUserProvider currentUserProvider;

	private final Clock clock;

	public SeatQueryService(SeatRepository seatRepository, ReservationRepository reservationRepository,
			PolicyService policyService, CurrentUserProvider currentUserProvider, Clock clock) {
		this.seatRepository = seatRepository;
		this.reservationRepository = reservationRepository;
		this.policyService = policyService;
		this.currentUserProvider = currentUserProvider;
		this.clock = clock;
	}

	/**
	 * 날짜·시간대의 좌석 그리드 (US-05 AC1·2).
	 *
	 * @param start 시작 시각 "HH:mm"
	 * @param end 종료 시각 "HH:mm" ("24:00" 은 다음 날 00:00)
	 * @throws BusinessException INVALID_RESERVATION_TIME(400), RESERVATION_DATE_OUT_OF_POLICY(400, AC4)
	 */
	public List<SeatStatusResponse> getSeats(LocalDate date, String start, String end) {
		Long userId = currentUserProvider.currentUserId();
		LocalDateTime startAt = atTime(date, start);
		LocalDateTime endAt = atTime(date, end);
		policyService.validateTimeSlot(startAt, endAt);
		policyService.validateReservableDate(date);

		Map<Long, List<SeatOccupancy>> occupancyBySeat = reservationRepository
			.findActiveOverlapping(startAt, endAt, LocalDateTime.now(clock))
			.stream()
			.collect(Collectors.groupingBy(SeatOccupancy::seatId));

		return seatRepository.findAllByActiveTrueOrderByGridRowAscGridColAsc()
			.stream()
			.map(seat -> SeatStatusResponse.of(seat,
					statusOf(occupancyBySeat.getOrDefault(seat.getSeatId(), List.of()), userId)))
			.toList();
	}

	private static SeatStatus statusOf(List<SeatOccupancy> occupancies, Long userId) {
		if (occupancies.isEmpty()) {
			return SeatStatus.AVAILABLE;
		}
		if (occupancies.stream().anyMatch(o -> o.userId().equals(userId))) {
			return SeatStatus.MINE;
		}
		if (occupancies.stream().anyMatch(o -> o.status() == ReservationStatus.CHECKED_IN)) {
			return SeatStatus.IN_USE;
		}
		return SeatStatus.RESERVED;
	}

	private static LocalDateTime atTime(LocalDate date, String text) {
		Matcher matcher = TIME.matcher(text.trim());
		if (matcher.matches()) {
			int hour = Integer.parseInt(matcher.group(1));
			int minute = Integer.parseInt(matcher.group(2));
			if (hour < 24 && minute < 60 || hour == 24 && minute == 0) {
				return date.atStartOfDay().plusHours(hour).plusMinutes(minute);
			}
		}
		throw new BusinessException(ErrorCode.INVALID_RESERVATION_TIME, "시각은 HH:mm 형식이어야 합니다.");
	}

}
