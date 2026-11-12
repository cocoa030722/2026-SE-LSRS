package com.se.lsrs.policy.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/** 정책 파라미터 (설계서 3장 PolicySetting). 코드 수정 없이 값만 바꿔 동작을 바꾼다 (US-07 AC5). */
@Entity
public class PolicySetting {

	@Id
	private String policyKey;

	@Column(nullable = false)
	private String policyValue;

	@Column(nullable = false)
	private LocalDateTime updatedAt;

	protected PolicySetting() {
	}

	public PolicySetting(String policyKey, String policyValue, LocalDateTime updatedAt) {
		this.policyKey = policyKey;
		this.policyValue = policyValue;
		this.updatedAt = updatedAt;
	}

	public void changeValue(String policyValue, LocalDateTime now) {
		this.policyValue = policyValue;
		this.updatedAt = now;
	}

	public String getPolicyKey() {
		return policyKey;
	}

	public String getPolicyValue() {
		return policyValue;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

}
