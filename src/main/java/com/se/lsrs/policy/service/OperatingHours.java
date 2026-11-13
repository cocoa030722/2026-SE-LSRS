package com.se.lsrs.policy.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 도서관 운영 시간 (PolicyKey OPEN_HOUR·CLOSE_HOUR). 예약은 이 안에서 정시 1시간 단위로 받는다 (설계서 3장).
 *
 * @param openHour 운영 시작 시각(시, 0~23)
 * @param closeHour 운영 종료 시각(시, 1~24). 24 는 다음 날 00:00
 */
public record OperatingHours(int openHour, int closeHour) {

	public LocalDateTime openAt(LocalDate date) {
		return date.atStartOfDay().plusHours(openHour);
	}

	public LocalDateTime closeAt(LocalDate date) {
		return date.atStartOfDay().plusHours(closeHour);
	}

	/** 예: "05:00~24:00" */
	public String label() {
		return "%02d:00~%02d:00".formatted(openHour, closeHour);
	}

}
