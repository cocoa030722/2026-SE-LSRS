package com.se.lsrs.policy.dto;

import jakarta.validation.constraints.NotBlank;

public record PolicyUpdateRequest(@NotBlank(message = "정책 값을 입력해 주세요.") String policyValue) {

}
