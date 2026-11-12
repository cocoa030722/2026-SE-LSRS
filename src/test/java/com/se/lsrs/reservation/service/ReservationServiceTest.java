package com.se.lsrs.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.se.lsrs.common.auth.CurrentUserProvider;
import com.se.lsrs.common.error.ErrorCode;
import com.se.lsrs.policy.domain.PolicyKey;
import com.se.lsrs.policy.service.PolicyService;
import com.se.lsrs.reservation.domain.CancelReason;
import com.se.lsrs.reservation.domain.Reservation;
import com.se.lsrs.reservation.domain.ReservationStatus;
import com.se.lsrs.reservation.dto.MyReservationResponse;
import com.se.lsrs.reservation.repository.ReservationRepository;
import com.se.lsrs.seat.domain.Seat;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

	private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

	private static final LocalDateTime NOW = LocalDateTime.of(2026, 12, 1, 10, 0);

	private static final Clock CLOCK = Clock.fixed(NOW.atZone(ZONE).toInstant(), ZONE);

	/** now - 체크인 가능 시간(15분) */
	private static final LocalDateTime START_AT_FROM = NOW.minusMinutes(15);

	private static final Long STUDENT_ID = 7L;

	@Mock
	private ReservationRepository reservationRepository;

	@Mock
	private PolicyService policyService;

	@Mock
	private CurrentUserProvider currentUserProvider;

	private ReservationService reservationService;

	@BeforeEach
	void setUp() {
		reservationService = new ReservationService(reservationRepository, policyService, currentUserProvider, CLOCK);
		lenient().when(currentUserProvider.currentUserId()).thenReturn(STUDENT_ID);
		lenient().when(policyService.getInt(PolicyKey.CHECKIN_WINDOW_MIN)).thenReturn(15);
	}

	private static Reservation reservation(long id, ReservationStatus status, LocalDateTime startAt) {
		Seat seat = mock(Seat.class);
		lenient().when(seat.getSeatNo()).thenReturn("A-01");
		Reservation reservation = mock(Reservation.class);
		lenient().when(reservation.getReservationId()).thenReturn(id);
		lenient().when(reservation.getSeat()).thenReturn(seat);
		lenient().when(reservation.getStatus()).thenReturn(status);
		lenient().when(reservation.getStartAt()).thenReturn(startAt);
		lenient().when(reservation.getEndAt()).thenReturn(startAt.plusHours(2));
		return reservation;
	}

	@Test
	void 내_예약을_상태_필터로_조회하고_취소_가능_여부를_표시한다_AC1_AC2() {
		List<Reservation> mine = List.of(reservation(3, ReservationStatus.RESERVED, NOW.plusHours(1)),
				reservation(2, ReservationStatus.RESERVED, NOW.minusMinutes(10)),
				reservation(1, ReservationStatus.RESERVED, NOW.minusMinutes(16)));
		given(reservationRepository.findMine(STUDENT_ID, ReservationStatus.RESERVED)).willReturn(mine);

		List<MyReservationResponse> result = reservationService.getMyReservations(ReservationStatus.RESERVED);

		assertThat(result).extracting(MyReservationResponse::reservationId).containsExactly(3L, 2L, 1L);
		assertThat(result).extracting(MyReservationResponse::cancellable).containsExactly(true, true, false);
		assertThat(result.get(0).seatNo()).isEqualTo("A-01");
	}

	@Test
	void 예약중_예약을_사유와_함께_취소한다_AC3() {
		Reservation reserved = reservation(1, ReservationStatus.RESERVED, NOW.plusHours(1));
		Reservation cancelled = reservation(1, ReservationStatus.CANCELLED, NOW.plusHours(1));
		given(reservationRepository.findByReservationIdAndUserUserId(1L, STUDENT_ID)).willReturn(Optional.of(reserved));
		given(reservationRepository.cancelByStudent(1L, CancelReason.SCHEDULE_CHANGE, START_AT_FROM)).willReturn(1);
		given(reservationRepository.findById(1L)).willReturn(Optional.of(cancelled));

		MyReservationResponse result = reservationService.cancelByStudent(1L, CancelReason.SCHEDULE_CHANGE);

		assertThat(result.status()).isEqualTo(ReservationStatus.CANCELLED);
		assertThat(result.cancellable()).isFalse();
		verify(reservationRepository).cancelByStudent(1L, CancelReason.SCHEDULE_CHANGE, START_AT_FROM);
	}

	@Test
	void 체크인_대기_예약도_취소할_수_있다() {
		Reservation waiting = reservation(1, ReservationStatus.RESERVED, START_AT_FROM);
		given(reservationRepository.findByReservationIdAndUserUserId(1L, STUDENT_ID)).willReturn(Optional.of(waiting));
		given(reservationRepository.cancelByStudent(1L, CancelReason.OTHER, START_AT_FROM)).willReturn(1);
		given(reservationRepository.findById(1L)).willReturn(Optional.of(waiting));

		reservationService.cancelByStudent(1L, CancelReason.OTHER);

		verify(reservationRepository).cancelByStudent(1L, CancelReason.OTHER, START_AT_FROM);
	}

	@Test
	void 체크인_가능_시간이_지난_예약은_취소할_수_없다() {
		Reservation expired = reservation(1, ReservationStatus.RESERVED, START_AT_FROM.minusMinutes(1));
		given(reservationRepository.findByReservationIdAndUserUserId(1L, STUDENT_ID)).willReturn(Optional.of(expired));

		assertThatThrownBy(() -> reservationService.cancelByStudent(1L, CancelReason.OTHER))
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESERVATION_NOT_CANCELLABLE)
			.hasMessageContaining("체크인 가능 시간");
		verify(reservationRepository, never()).cancelByStudent(anyLong(), any(), any());
	}

	@ParameterizedTest
	@EnumSource(value = ReservationStatus.class, names = { "CANCELLED", "CHECKED_IN", "AUTO_CANCELLED" })
	void 이미_종결된_예약은_취소할_수_없다_AC5(ReservationStatus status) {
		Reservation done = reservation(1, status, NOW.plusHours(1));
		given(reservationRepository.findByReservationIdAndUserUserId(1L, STUDENT_ID)).willReturn(Optional.of(done));

		assertThatThrownBy(() -> reservationService.cancelByStudent(1L, CancelReason.OTHER))
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESERVATION_NOT_CANCELLABLE);
		verify(reservationRepository, never()).cancelByStudent(anyLong(), any(), any());
	}

	@Test
	void 다른_학생의_예약은_찾을_수_없다고_응답한다_AC5() {
		given(reservationRepository.findByReservationIdAndUserUserId(99L, STUDENT_ID)).willReturn(Optional.empty());

		assertThatThrownBy(() -> reservationService.cancelByStudent(99L, CancelReason.OTHER))
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESERVATION_NOT_FOUND);
	}

	@Test
	void 조회_후_상태가_바뀌어_조건부_UPDATE가_0건이면_거부한다() {
		Reservation reserved = reservation(1, ReservationStatus.RESERVED, NOW.plusHours(1));
		given(reservationRepository.findByReservationIdAndUserUserId(1L, STUDENT_ID)).willReturn(Optional.of(reserved));
		given(reservationRepository.cancelByStudent(1L, CancelReason.OTHER, START_AT_FROM)).willReturn(0);

		assertThatThrownBy(() -> reservationService.cancelByStudent(1L, CancelReason.OTHER))
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESERVATION_NOT_CANCELLABLE);
	}

}
