package com.se.lsrs.reservation.dto;

import java.time.LocalDateTime;

import com.se.lsrs.reservation.domain.CancelReason;
import com.se.lsrs.reservation.domain.Reservation;
import com.se.lsrs.reservation.domain.ReservationStatus;

/**
 * 내 예약 항목 (US-08 AC1).
 *
 * @param cancellable 지금 학생이 직접 취소할 수 있는지 (화면의 취소 버튼 표시용)
 */
public record MyReservationResponse(Long reservationId, String seatNo, LocalDateTime startAt, LocalDateTime endAt,
		ReservationStatus status, CancelReason cancelReason, boolean cancellable) {

	public static MyReservationResponse of(Reservation reservation, boolean cancellable) {
		return new MyReservationResponse(reservation.getReservationId(), reservation.getSeat().getSeatNo(),
				reservation.getStartAt(), reservation.getEndAt(), reservation.getStatus(),
				reservation.getCancelReason(), cancellable);
	}

}
