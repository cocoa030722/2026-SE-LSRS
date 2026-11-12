package com.se.lsrs.policy.dto;

import java.time.LocalDate;

import com.se.lsrs.policy.domain.ExamPeriod;

public record ExamPeriodResponse(Long examPeriodId, LocalDate startDate, LocalDate endDate) {

	public static ExamPeriodResponse from(ExamPeriod examPeriod) {
		return new ExamPeriodResponse(examPeriod.getExamPeriodId(), examPeriod.getStartDate(),
				examPeriod.getEndDate());
	}

}
