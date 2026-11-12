package com.se.lsrs.policy.dto;

import java.time.LocalDateTime;

import com.se.lsrs.policy.domain.PolicySetting;

public record PolicySettingResponse(String policyKey, String policyValue, LocalDateTime updatedAt) {

	public static PolicySettingResponse from(PolicySetting setting) {
		return new PolicySettingResponse(setting.getPolicyKey(), setting.getPolicyValue(), setting.getUpdatedAt());
	}

}
