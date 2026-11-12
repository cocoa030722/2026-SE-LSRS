package com.se.lsrs.policy.dto;

import java.time.LocalDate;

import com.se.lsrs.policy.service.AppliedPolicy;

/** GET /api/policies/current 응답: 적용 정책·시험 기간 여부 (US-07 AC3) */
public record CurrentPolicyResponse(LocalDate targetDate, boolean examPeriod, LocalDate reservableFrom,
		LocalDate reservableUntil, int maxHours) {

	public static CurrentPolicyResponse from(AppliedPolicy policy) {
		return new CurrentPolicyResponse(policy.targetDate(), policy.examPeriod(), policy.reservableFrom(),
				policy.reservableUntil(), policy.maxHours());
	}

}
