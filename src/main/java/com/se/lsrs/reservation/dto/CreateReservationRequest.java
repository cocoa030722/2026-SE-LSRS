package com.se.lsrs.reservation.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

/**
 * 예약 생성 요청 (US-06 AC1·2). 시각은 정시 ISO-8601 (예: "2026-12-01T13:00:00").
 * 운영 종료 24:00 은 다음 날 00:00 으로 보낸다.
 */
public record CreateReservationRequest(
		@NotNull(message = "좌석을 선택해 주세요.") Long seatId,
		@NotNull(message = "시작 시각을 선택해 주세요.") LocalDateTime startAt,
		@NotNull(message = "종료 시각을 선택해 주세요.") LocalDateTime endAt) {

}
