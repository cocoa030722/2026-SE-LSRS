package com.se.lsrs.policy.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.se.lsrs.common.auth.CurrentUserProvider;
import com.se.lsrs.common.error.BusinessException;
import com.se.lsrs.common.error.ErrorCode;
import com.se.lsrs.policy.domain.ExamPeriod;
import com.se.lsrs.policy.repository.ExamPeriodRepository;
import com.se.lsrs.user.repository.UserRepository;

/** 시험 기간 등록·조회·삭제 (US-07, FR-A-20) */
@Service
@Transactional(readOnly = true)
public class ExamPeriodService {

	private final ExamPeriodRepository examPeriodRepository;

	private final UserRepository userRepository;

	private final CurrentUserProvider currentUserProvider;

	public ExamPeriodService(ExamPeriodRepository examPeriodRepository, UserRepository userRepository,
			CurrentUserProvider currentUserProvider) {
		this.examPeriodRepository = examPeriodRepository;
		this.userRepository = userRepository;
		this.currentUserProvider = currentUserProvider;
	}

	/**
	 * @throws BusinessException INVALID_EXAM_PERIOD(400, 시작일 > 종료일, US-07 AC4),
	 * EXAM_PERIOD_OVERLAP(409, 기존 기간과 겹침)
	 */
	@Transactional
	public ExamPeriod create(LocalDate startDate, LocalDate endDate) {
		if (startDate.isAfter(endDate)) {
			throw new BusinessException(ErrorCode.INVALID_EXAM_PERIOD);
		}
		if (examPeriodRepository.existsOverlapping(startDate, endDate)) {
			throw new BusinessException(ErrorCode.EXAM_PERIOD_OVERLAP);
		}
		Long adminId = currentUserProvider.currentUserId();
		return examPeriodRepository.save(new ExamPeriod(startDate, endDate, userRepository.getReferenceById(adminId)));
	}

	public List<ExamPeriod> findAll() {
		return examPeriodRepository.findAllByOrderByStartDateAsc();
	}

	/** @throws BusinessException EXAM_PERIOD_NOT_FOUND(404) */
	@Transactional
	public void delete(Long examPeriodId) {
		ExamPeriod examPeriod = examPeriodRepository.findById(examPeriodId)
			.orElseThrow(() -> new BusinessException(ErrorCode.EXAM_PERIOD_NOT_FOUND));
		examPeriodRepository.delete(examPeriod);
	}

}
