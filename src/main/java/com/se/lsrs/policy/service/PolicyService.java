package com.se.lsrs.policy.service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.se.lsrs.common.error.BusinessException;
import com.se.lsrs.common.error.ErrorCode;
import com.se.lsrs.policy.domain.PolicyKey;
import com.se.lsrs.policy.domain.PolicySetting;
import com.se.lsrs.policy.repository.ExamPeriodRepository;
import com.se.lsrs.policy.repository.PolicySettingRepository;

/**
 * 예약 정책 판정과 정책 파라미터 관리 (US-07). 정책 로직은 이 서비스에 모은다 (NFR 4.1).
 *
 * 정책 판정 (설계서 3장 Business Rules):
 * 오늘 또는 예약 대상일이 시험 기간이면 시험 정책(대상일 ≤ 오늘+EXAM_DAYS, 최대 EXAM_MAX_HOURS),
 * 아니면 평상시 정책(대상일 ≤ 오늘+NORMAL_DAYS, 최대 NORMAL_MAX_HOURS)을 적용한다.
 * 정책은 예약 생성 시점에만 검사하므로 값 변경은 이후 생성되는 예약부터 적용된다 (US-07 AC5).
 */
@Service
@Transactional(readOnly = true)
public class PolicyService {

	private final ExamPeriodRepository examPeriodRepository;

	private final PolicySettingRepository policySettingRepository;

	private final Clock clock;

	public PolicyService(ExamPeriodRepository examPeriodRepository, PolicySettingRepository policySettingRepository,
			Clock clock) {
		this.examPeriodRepository = examPeriodRepository;
		this.policySettingRepository = policySettingRepository;
		this.clock = clock;
	}

	/** 오늘 기준 적용 정책 (GET /api/policies/current) */
	public AppliedPolicy currentPolicy() {
		return policyFor(LocalDate.now(clock));
	}

	/** 예약 대상일에 적용되는 정책 */
	public AppliedPolicy policyFor(LocalDate targetDate) {
		LocalDate today = LocalDate.now(clock);
		boolean exam = examPeriodRepository.existsCovering(today) || examPeriodRepository.existsCovering(targetDate);
		int days = getInt(exam ? PolicyKey.EXAM_DAYS : PolicyKey.NORMAL_DAYS);
		int maxHours = getInt(exam ? PolicyKey.EXAM_MAX_HOURS : PolicyKey.NORMAL_MAX_HOURS);
		return new AppliedPolicy(targetDate, exam, today, today.plusDays(days), maxHours);
	}

	/**
	 * 예약 요청이 적용 정책을 벗어나면 거부한다 (US-07 AC2).
	 * 예약 생성(US-06, ReservationService.reserve)이 호출한다. 시각 형식·운영 시간 등 그 밖의 검증은 호출 측 책임이다.
	 *
	 * @throws BusinessException RESERVATION_DATE_OUT_OF_POLICY, RESERVATION_DURATION_EXCEEDED (400)
	 */
	public void validateReservation(LocalDateTime startAt, LocalDateTime endAt) {
		AppliedPolicy policy = policyFor(startAt.toLocalDate());
		String policyName = policy.examPeriod() ? "시험 기간" : "평상시";

		if (!policy.isReservableDate(policy.targetDate())) {
			throw new BusinessException(ErrorCode.RESERVATION_DATE_OUT_OF_POLICY,
					"%s 예약 가능 기간은 %s ~ %s 입니다.".formatted(policyName, policy.reservableFrom(),
							policy.reservableUntil()));
		}
		if (Duration.between(startAt, endAt).compareTo(Duration.ofHours(policy.maxHours())) > 0) {
			throw new BusinessException(ErrorCode.RESERVATION_DURATION_EXCEEDED,
					"%s 최대 이용 시간은 %d시간입니다.".formatted(policyName, policy.maxHours()));
		}
	}

	/** 정수형 정책 값. 값이 없거나 숫자가 아니면 설정 오류다. */
	public int getInt(PolicyKey key) {
		String value = policySettingRepository.findById(key.name())
			.map(PolicySetting::getPolicyValue)
			.orElseThrow(() -> new IllegalStateException("정책 값이 설정되지 않았습니다: " + key));
		try {
			return Integer.parseInt(value.trim());
		}
		catch (NumberFormatException e) {
			throw new IllegalStateException("정책 값이 정수가 아닙니다: " + key + "=" + value, e);
		}
	}

	/** 정책 파라미터 목록 (관리자) */
	public List<PolicySetting> getSettings() {
		return policySettingRepository.findAllByOrderByPolicyKeyAsc();
	}

	/**
	 * 정책 파라미터 변경 (PUT /api/admin/policies/{key}, US-07 AC5).
	 *
	 * @throws BusinessException POLICY_NOT_FOUND(404), INVALID_POLICY_VALUE(400)
	 */
	@Transactional
	public PolicySetting updateSetting(String policyKey, String policyValue) {
		PolicySetting setting = policySettingRepository.findById(policyKey)
			.orElseThrow(() -> new BusinessException(ErrorCode.POLICY_NOT_FOUND));
		String value = policyValue.trim();
		PolicyKey.find(policyKey).ifPresent(key -> validateIntValue(key, value));
		setting.changeValue(value, LocalDateTime.now(clock));
		return setting;
	}

	private void validateIntValue(PolicyKey key, String value) {
		boolean valid;
		try {
			valid = Integer.parseInt(value) >= key.minValue();
		}
		catch (NumberFormatException e) {
			valid = false;
		}
		if (!valid) {
			throw new BusinessException(ErrorCode.INVALID_POLICY_VALUE,
					"%s 값은 %d 이상의 정수여야 합니다.".formatted(key.name(), key.minValue()));
		}
	}

}
