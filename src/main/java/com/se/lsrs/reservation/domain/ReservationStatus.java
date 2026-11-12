package com.se.lsrs.reservation.domain;

/** 예약 상태. RESERVED → CHECKED_IN / CANCELLED / AUTO_CANCELLED 로만 전이한다 (설계서 3장). */
public enum ReservationStatus {
	RESERVED, CHECKED_IN, CANCELLED, AUTO_CANCELLED
}
