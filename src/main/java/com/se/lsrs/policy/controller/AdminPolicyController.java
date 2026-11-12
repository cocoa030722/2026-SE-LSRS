package com.se.lsrs.policy.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.se.lsrs.policy.dto.PolicySettingResponse;
import com.se.lsrs.policy.dto.PolicyUpdateRequest;
import com.se.lsrs.policy.service.PolicyService;

import jakarta.validation.Valid;

/**
 * 정책 파라미터 조회·변경 (설계서 4장 PUT /api/admin/policies/{key}, US-07 AC5).
 * GET 목록은 관리 화면에서 현재 값을 보여 주기 위해 추가했다(설계서 목록에는 없음).
 */
@RestController
@RequestMapping("/api/admin/policies")
public class AdminPolicyController {

	private final PolicyService policyService;

	public AdminPolicyController(PolicyService policyService) {
		this.policyService = policyService;
	}

	@GetMapping
	public List<PolicySettingResponse> findAll() {
		return policyService.getSettings().stream().map(PolicySettingResponse::from).toList();
	}

	@PutMapping("/{policyKey}")
	public PolicySettingResponse update(@PathVariable String policyKey,
			@Valid @RequestBody PolicyUpdateRequest request) {
		return PolicySettingResponse.from(policyService.updateSetting(policyKey, request.policyValue()));
	}

}
