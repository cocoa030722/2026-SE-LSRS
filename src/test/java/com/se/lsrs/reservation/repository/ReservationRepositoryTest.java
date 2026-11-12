package com.se.lsrs.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.se.lsrs.TestcontainersConfiguration;
import com.se.lsrs.reservation.domain.CancelReason;
import com.se.lsrs.reservation.domain.CanceledBy;
import com.se.lsrs.reservation.domain.Reservation;
import com.se.lsrs.reservation.domain.ReservationStatus;

/** MySQL 에서 조건부 UPDATE·정렬·본인 조회를 검증한다 (US-08). */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
@Testcontainers(disabledWithoutDocker = true)
@Sql("/fixture-reservations.sql")
class ReservationRepositoryTest {

	private static final LocalDateTime NOW_MINUS_WINDOW = LocalDateTime.of(2026, 12, 1, 10, 0);

	@Autowired
	private ReservationRepository reservationRepository;

	@Test
	void 내_예약만_최신순으로_조회한다_AC1() {
		assertThat(reservationRepository.findMine(1L, null)).extracting(Reservation::getReservationId)
			.containsExactly(11L, 10L, 12L, 14L);
	}

	@Test
	void 상태로_필터링한다_AC2() {
		assertThat(reservationRepository.findMine(1L, ReservationStatus.AUTO_CANCELLED))
			.extracting(Reservation::getReservationId)
			.containsExactly(14L);
		assertThat(reservationRepository.findMine(1L, ReservationStatus.RESERVED).get(0).getSeat().getSeatNo())
			.isEqualTo("A-02");
	}

	@Test
	void 다른_학생의_예약은_본인_조회로_찾을_수_없다() {
		assertThat(reservationRepository.findByReservationIdAndUserUserId(13L, 1L)).isEmpty();
		assertThat(reservationRepository.findByReservationIdAndUserUserId(11L, 1L)).isPresent();
	}

	@Test
	void RESERVED_예약을_사유와_함께_취소한다_AC3_AC4() {
		int updated = reservationRepository.cancelByStudent(11L, CancelReason.TIME_CHANGE, NOW_MINUS_WINDOW);

		assertThat(updated).isEqualTo(1);
		Reservation cancelled = reservationRepository.findById(11L).orElseThrow();
		assertThat(cancelled.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
		assertThat(cancelled.getCancelReason()).isEqualTo(CancelReason.TIME_CHANGE);
		assertThat(cancelled.getCanceledBy()).isEqualTo(CanceledBy.STUDENT);
		assertThat(cancelled.isPenaltyApplied()).isFalse();
	}

	@Test
	void 이미_취소된_예약은_다시_반영되지_않는다() {
		reservationRepository.cancelByStudent(11L, CancelReason.OTHER, NOW_MINUS_WINDOW);

		assertThat(reservationRepository.cancelByStudent(11L, CancelReason.OTHER, NOW_MINUS_WINDOW)).isZero();
	}

	@Test
	void 종결_상태의_예약은_반영되지_않는다_AC5() {
		assertThat(reservationRepository.cancelByStudent(10L, CancelReason.OTHER, NOW_MINUS_WINDOW)).isZero();
		assertThat(reservationRepository.cancelByStudent(14L, CancelReason.OTHER, LocalDateTime.of(2026, 1, 1, 0, 0)))
			.isZero();
		assertThat(reservationRepository.findById(10L).orElseThrow().getStatus())
			.isEqualTo(ReservationStatus.CHECKED_IN);
	}

	@Test
	void 체크인_가능_시간이_지난_RESERVED_는_반영되지_않는다() {
		LocalDateTime afterWindow = LocalDateTime.of(2026, 12, 2, 13, 1);

		assertThat(reservationRepository.cancelByStudent(11L, CancelReason.OTHER, afterWindow)).isZero();
		assertThat(reservationRepository.findById(11L).orElseThrow().getStatus())
			.isEqualTo(ReservationStatus.RESERVED);
	}

}
