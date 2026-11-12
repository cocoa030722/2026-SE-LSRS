package com.se.lsrs.policy.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.se.lsrs.policy.dto.CurrentPolicyResponse;
import com.se.lsrs.policy.service.PolicyService;

/** 적용 정책 조회 (설계서 4장 GET /api/policies/current, US-07 AC3) */
@RestController
@RequestMapping("/api/policies")
public class PolicyController {

	private final PolicyService policyService;

	public PolicyController(PolicyService policyService) {
		this.policyService = policyService;
	}

	/**
	 * @param date 예약 대상일(선택). 생략하면 오늘. 대상일이 시험 기간이면 시험 정책이 적용된다.
	 */
	@GetMapping("/current")
	public CurrentPolicyResponse current(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		return CurrentPolicyResponse.from(date == null ? policyService.currentPolicy() : policyService.policyFor(date),
				policyService.operatingHours());
	}

}
