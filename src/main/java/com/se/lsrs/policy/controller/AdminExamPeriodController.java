package com.se.lsrs.policy.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.se.lsrs.policy.dto.ExamPeriodRequest;
import com.se.lsrs.policy.dto.ExamPeriodResponse;
import com.se.lsrs.policy.service.ExamPeriodService;

import jakarta.validation.Valid;

/**
 * 시험 기간 등록·조회·삭제 (설계서 4장 /api/admin/exam-periods, US-07).
 * /api/admin/** 의 관리자 권한 검사는 SecurityConfig(US-02)가 담당한다.
 */
@RestController
@RequestMapping("/api/admin/exam-periods")
public class AdminExamPeriodController {

	private final ExamPeriodService examPeriodService;

	public AdminExamPeriodController(ExamPeriodService examPeriodService) {
		this.examPeriodService = examPeriodService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ExamPeriodResponse create(@Valid @RequestBody ExamPeriodRequest request) {
		return ExamPeriodResponse.from(examPeriodService.create(request.startDate(), request.endDate()));
	}

	@GetMapping
	public List<ExamPeriodResponse> findAll() {
		return examPeriodService.findAll().stream().map(ExamPeriodResponse::from).toList();
	}

	@DeleteMapping("/{examPeriodId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long examPeriodId) {
		examPeriodService.delete(examPeriodId);
	}

}
