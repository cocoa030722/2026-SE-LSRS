package com.se.lsrs.policy.dto;

import java.time.LocalDate;

import com.se.lsrs.policy.service.AppliedPolicy;
import com.se.lsrs.policy.service.OperatingHours;

/**
 * GET /api/policies/current 응답: 적용 정책·시험 기간 여부 (US-07 AC3).
 * 운영 시간은 좌석 조회·예약 화면의 시간대 선택지(US-05·06)에 쓴다.
 *
 * @param openHour 운영 시작 시각(시)
 * @param closeHour 운영 종료 시각(시, 24 는 다음 날 00:00)
 */
public record CurrentPolicyResponse(LocalDate targetDate, boolean examPeriod, LocalDate reservableFrom,
		LocalDate reservableUntil, int maxHours, int openHour, int closeHour) {

	public static CurrentPolicyResponse from(AppliedPolicy policy, OperatingHours hours) {
		return new CurrentPolicyResponse(policy.targetDate(), policy.examPeriod(), policy.reservableFrom(),
				policy.reservableUntil(), policy.maxHours(), hours.openHour(), hours.closeHour());
	}

}
