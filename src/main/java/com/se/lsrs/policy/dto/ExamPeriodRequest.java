package com.se.lsrs.policy.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record ExamPeriodRequest(
		@NotNull(message = "시작일을 입력해 주세요.") LocalDate startDate,
		@NotNull(message = "종료일을 입력해 주세요.") LocalDate endDate) {

}
