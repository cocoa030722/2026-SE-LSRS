package com.se.lsrs.policy.domain;

import java.time.LocalDate;

import com.se.lsrs.user.domain.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/** 시험 기간 (설계서 3장 ExamPeriod). startDate ≤ endDate, 기존 기간과 겹치지 않는다. */
@Entity
public class ExamPeriod {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long examPeriodId;

	@Column(nullable = false)
	private LocalDate startDate;

	@Column(nullable = false)
	private LocalDate endDate;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "created_by", nullable = false)
	private User createdBy;

	protected ExamPeriod() {
	}

	public ExamPeriod(LocalDate startDate, LocalDate endDate, User createdBy) {
		this.startDate = startDate;
		this.endDate = endDate;
		this.createdBy = createdBy;
	}

	public Long getExamPeriodId() {
		return examPeriodId;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

}
