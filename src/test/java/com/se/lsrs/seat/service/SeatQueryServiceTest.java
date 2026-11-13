package com.se.lsrs.seat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

@ExtendWith(MockitoExtension.class)
class SeatQueryServiceTest {

	private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

	private static final LocalDateTime NOW = LocalDateTime.of(2026, 12, 1, 10, 0);

	private static final Clock CLOCK = Clock.fixed(NOW.atZone(ZONE).toInstant(), ZONE);

	private static final LocalDate DATE = NOW.toLocalDate();

	private static final Long ME = 1L;

	private static final Long OTHER = 2L;

	@Mock
	private SeatRepository seatRepository;

	@Mock
	private ReservationRepository reservationRepository;

	@Mock
	private PolicyService policyService;

	@Mock
	private CurrentUserProvider currentUserProvider;

	private SeatQueryService seatQueryService;

	@BeforeEach
	void setUp() {
		seatQueryService = new SeatQueryService(seatRepository, reservationRepository, policyService,
				currentUserProvider, CLOCK);
		lenient().when(currentUserProvider.currentUserId()).thenReturn(ME);
	}

	private static Seat seat(long id, int col) {
		Seat seat = mock(Seat.class);
		lenient().when(seat.getSeatId()).thenReturn(id);
		lenient().when(seat.getSeatNo()).thenReturn("A-0" + col);
		lenient().when(seat.getGridRow()).thenReturn(1);
		lenient().when(seat.getGridCol()).thenReturn(col);
		return seat;
	}

	@Test
	void 진행_중_예약으로_4가지_상태를_계산한다_AC1_AC2() {
		LocalDateTime start = DATE.atTime(13, 0);
		LocalDateTime end = DATE.atTime(15, 0);
		List<Seat> grid = List.of(seat(1, 1), seat(2, 2), seat(3, 3), seat(4, 4), seat(5, 5));
		given(seatRepository.findAllByActiveTrueOrderByGridRowAscGridColAsc()).willReturn(grid);
		given(reservationRepository.findActiveOverlapping(start, end, NOW)).willReturn(List.of(
				new SeatOccupancy(2L, ME, ReservationStatus.RESERVED),
				new SeatOccupancy(3L, OTHER, ReservationStatus.CHECKED_IN),
				new SeatOccupancy(4L, OTHER, ReservationStatus.RESERVED),
				// 같은 좌석에 다른 학생의 사용 중 + 내 예약이 겹치면 '내 예약'을 우선 표시
				new SeatOccupancy(5L, OTHER, ReservationStatus.CHECKED_IN),
				new SeatOccupancy(5L, ME, ReservationStatus.RESERVED)));

		List<SeatStatusResponse> seats = seatQueryService.getSeats(DATE, "13:00", "15:00");

		assertThat(seats).extracting(SeatStatusResponse::status)
			.containsExactly(SeatStatus.AVAILABLE, SeatStatus.MINE, SeatStatus.IN_USE, SeatStatus.RESERVED,
					SeatStatus.MINE);
		assertThat(seats.get(0).gridCol()).isEqualTo(1);
		verify(policyService).validateTimeSlot(start, end);
		verify(policyService).validateReservableDate(DATE);
	}

	@Test
	void 종료_24시는_다음_날_0시로_해석한다() {
		given(seatRepository.findAllByActiveTrueOrderByGridRowAscGridColAsc()).willReturn(List.of());

		seatQueryService.getSeats(DATE, "22:00", "24:00");

		verify(reservationRepository).findActiveOverlapping(DATE.atTime(22, 0), DATE.plusDays(1).atStartOfDay(), NOW);
	}

	@Test
	void 시각_형식이_틀리면_400() {
		for (String bad : List.of("13", "25:00", "24:30", "13:60", "ab:cd")) {
			assertThatThrownBy(() -> seatQueryService.getSeats(DATE, bad, "15:00"))
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_RESERVATION_TIME);
		}
		verify(reservationRepository, never()).findActiveOverlapping(any(), any(), any());
	}

	@Test
	void 예약_가능_범위_밖의_날짜는_조회하지_않는다_AC4() {
		LocalDate far = DATE.plusDays(20);
		willThrow(new BusinessException(ErrorCode.RESERVATION_DATE_OUT_OF_POLICY)).given(policyService)
			.validateReservableDate(far);

		assertThatThrownBy(() -> seatQueryService.getSeats(far, "13:00", "15:00"))
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESERVATION_DATE_OUT_OF_POLICY);
		verify(seatRepository, never()).findAllByActiveTrueOrderByGridRowAscGridColAsc();
	}

}
