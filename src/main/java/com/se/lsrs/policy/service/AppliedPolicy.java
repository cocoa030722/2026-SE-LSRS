package com.se.lsrs.policy.service;

import java.time.LocalDate;

/**
 * 특정 예약 대상일에 적용되는 정책 (FR-A-20).
 *
 * @param targetDate 예약 대상일
 * @param examPeriod 시험 기간 정책 적용 여부
 * @param reservableFrom 예약 가능 첫날(오늘)
 * @param reservableUntil 예약 가능 마지막 날
 * @param maxHours 최대 이용 시간(시간)
 */
public record AppliedPolicy(LocalDate targetDate, boolean examPeriod, LocalDate reservableFrom,
		LocalDate reservableUntil, int maxHours) {

	public boolean isReservableDate(LocalDate date) {
		return !date.isBefore(reservableFrom) && !date.isAfter(reservableUntil);
	}

}
