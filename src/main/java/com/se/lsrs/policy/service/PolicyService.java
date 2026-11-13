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

	/** 운영 시간 (OPEN_HOUR·CLOSE_HOUR) */
	public OperatingHours operatingHours() {
		return new OperatingHours(getInt(PolicyKey.OPEN_HOUR), getInt(PolicyKey.CLOSE_HOUR));
	}

	/**
	 * 예약 요청이 적용 정책을 벗어나면 거부한다 (US-07 AC2). 예약 생성(US-06, ReservationService.reserve)이 호출한다.
	 * 시간대(정시 1시간 단위·운영 시간) → 예약 가능 기간 → 최대 이용 시간 순으로 검사한다.
	 *
	 * @throws BusinessException INVALID_RESERVATION_TIME, RESERVATION_DATE_OUT_OF_POLICY,
	 * RESERVATION_DURATION_EXCEEDED (400)
	 */
	public void validateReservation(LocalDateTime startAt, LocalDateTime endAt) {
		validateTimeSlot(startAt, endAt);
		AppliedPolicy policy = validateReservableDate(startAt.toLocalDate());

		if (Duration.between(startAt, endAt).compareTo(Duration.ofHours(policy.maxHours())) > 0) {
			throw new BusinessException(ErrorCode.RESERVATION_DURATION_EXCEEDED,
					"%s 최대 이용 시간은 %d시간입니다.".formatted(policyName(policy), policy.maxHours()));
		}
	}

	/**
	 * 예약 대상일이 예약 가능 기간 안인지 검사한다 (US-05 AC4, US-07 AC2).
	 *
	 * @return 대상일에 적용되는 정책
	 * @throws BusinessException RESERVATION_DATE_OUT_OF_POLICY (400)
	 */
	public AppliedPolicy validateReservableDate(LocalDate targetDate) {
		AppliedPolicy policy = policyFor(targetDate);
		if (!policy.isReservableDate(targetDate)) {
			throw new BusinessException(ErrorCode.RESERVATION_DATE_OUT_OF_POLICY,
					"%s 예약 가능 기간은 %s ~ %s 입니다.".formatted(policyName(policy), policy.reservableFrom(),
							policy.reservableUntil()));
		}
		return policy;
	}

	/**
	 * 시간대가 시작 &lt; 종료, 정시 1시간 단위, 시작일의 운영 시간 안인지 검사한다 (설계서 3장).
	 * 좌석 조회(US-05)와 예약 생성(US-06)이 같은 규칙을 쓴다.
	 *
	 * @throws BusinessException INVALID_RESERVATION_TIME (400)
	 */
	public void validateTimeSlot(LocalDateTime startAt, LocalDateTime endAt) {
		if (!startAt.isBefore(endAt)) {
			throw new BusinessException(ErrorCode.INVALID_RESERVATION_TIME, "종료 시각은 시작 시각보다 늦어야 합니다.");
		}
		if (!isOnTheHour(startAt) || !isOnTheHour(endAt)) {
			throw new BusinessException(ErrorCode.INVALID_RESERVATION_TIME, "예약은 정시 1시간 단위로만 할 수 있습니다.");
		}
		OperatingHours hours = operatingHours();
		LocalDate date = startAt.toLocalDate();
		if (startAt.isBefore(hours.openAt(date)) || endAt.isAfter(hours.closeAt(date))) {
			throw new BusinessException(ErrorCode.INVALID_RESERVATION_TIME,
					"운영 시간(%s) 안에서만 예약할 수 있습니다.".formatted(hours.label()));
		}
	}

	private static boolean isOnTheHour(LocalDateTime time) {
		return time.getMinute() == 0 && time.getSecond() == 0 && time.getNano() == 0;
	}

	private static String policyName(AppliedPolicy policy) {
		return policy.examPeriod() ? "시험 기간" : "평상시";
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
			int parsed = Integer.parseInt(value);
			valid = parsed >= key.minValue() && parsed <= key.maxValue();
		}
		catch (NumberFormatException e) {
			valid = false;
		}
		if (!valid) {
			String range = key.maxValue() == Integer.MAX_VALUE ? "%d 이상의".formatted(key.minValue())
					: "%d~%d 사이의".formatted(key.minValue(), key.maxValue());
			throw new BusinessException(ErrorCode.INVALID_POLICY_VALUE,
					"%s 값은 %s 정수여야 합니다.".formatted(key.name(), range));
		}
	}

}
