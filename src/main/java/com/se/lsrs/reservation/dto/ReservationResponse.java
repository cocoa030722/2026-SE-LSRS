package com.se.lsrs.reservation.dto;

import java.time.LocalDateTime;

import com.se.lsrs.reservation.domain.Reservation;
import com.se.lsrs.reservation.domain.ReservationStatus;

/** 예약 생성 결과. 확인 화면에 좌석·시간 정보를 표시한다 (US-06 AC1). */
public record ReservationResponse(Long reservationId, Long seatId, String seatNo, LocalDateTime startAt,
		LocalDateTime endAt, ReservationStatus status, LocalDateTime createdAt) {

	public static ReservationResponse from(Reservation reservation) {
		return new ReservationResponse(reservation.getReservationId(), reservation.getSeat().getSeatId(),
				reservation.getSeat().getSeatNo(), reservation.getStartAt(), reservation.getEndAt(),
				reservation.getStatus(), reservation.getCreatedAt());
	}

}
