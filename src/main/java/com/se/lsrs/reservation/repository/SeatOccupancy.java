package com.se.lsrs.reservation.repository;

import com.se.lsrs.reservation.domain.ReservationStatus;

/** 좌석을 점유한 진행 중 예약의 요약 (좌석 상태 계산용, US-05) */
public record SeatOccupancy(Long seatId, Long userId, ReservationStatus status) {

}
