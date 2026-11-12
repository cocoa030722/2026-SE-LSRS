package com.se.lsrs.policy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.se.lsrs.common.error.BusinessException;
import com.se.lsrs.common.error.ErrorCode;
import com.se.lsrs.policy.domain.PolicySetting;
import com.se.lsrs.policy.repository.ExamPeriodRepository;
import com.se.lsrs.policy.repository.PolicySettingRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PolicyServiceTest {

	private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

	/** 오늘 = 2026-12-01 10:00 */
	private static final LocalDate TODAY = LocalDate.of(2026, 12, 1);

	private static final Clock CLOCK = Clock.fixed(TODAY.atTime(10, 0).atZone(ZONE).toInstant(), ZONE);

	@Mock
	private ExamPeriodRepository examPeriodRepository;

	@Mock
	private PolicySettingRepository policySettingRepository;

	private PolicyService policyService;

	private final Map<String, String> settings = new HashMap<>();

	private final Set<LocalDate> examDates = new HashSet<>();

	@BeforeEach
	void setUp() {
		policyService = new PolicyService(examPeriodRepository, policySettingRepository, CLOCK);
		settings.putAll(Map.of("NORMAL_DAYS", "7", "NORMAL_MAX_HOURS", "4", "EXAM_DAYS", "1", "EXAM_MAX_HOURS", "3",
				"CHECKIN_WINDOW_MIN", "15"));
		given(policySettingRepository.findById(any())).willAnswer(invocation -> {
			String key = invocation.getArgument(0);
			return Optional.ofNullable(settings.get(key)).map(value -> new PolicySetting(key, value, null));
		});
		given(examPeriodRepository.existsCovering(any()))
			.willAnswer(invocation -> examDates.contains(invocation.getArgument(0, LocalDate.class)));
	}

	private void examDays(Set<LocalDate> dates) {
		examDates.addAll(dates);
	}

	private static LocalDateTime at(LocalDate date, int hour) {
		return date.atTime(hour, 0);
	}

	@Nested
	@DisplayName("정책 판정 (US-07 AC1)")
	class PolicyFor {

		@Test
		void 평상시에는_오늘부터_7일_최대_4시간() {
			AppliedPolicy policy = policyService.currentPolicy();

			assertThat(policy.examPeriod()).isFalse();
			assertThat(policy.reservableFrom()).isEqualTo(TODAY);
			assertThat(policy.reservableUntil()).isEqualTo(TODAY.plusDays(7));
			assertThat(policy.maxHours()).isEqualTo(4);
		}

		@Test
		void 오늘이_시험_기간이면_당일_다음날까지_최대_3시간() {
			examDays(Set.of(TODAY));

			AppliedPolicy policy = policyService.policyFor(TODAY.plusDays(5));

			assertThat(policy.examPeriod()).isTrue();
			assertThat(policy.reservableUntil()).isEqualTo(TODAY.plusDays(1));
			assertThat(policy.maxHours()).isEqualTo(3);
		}

		@Test
		void 대상일이_시험_기간이면_오늘이_평상시여도_시험_정책() {
			examDays(Set.of(TODAY.plusDays(3)));

			AppliedPolicy policy = policyService.policyFor(TODAY.plusDays(3));

			assertThat(policy.examPeriod()).isTrue();
			assertThat(policy.isReservableDate(TODAY.plusDays(3))).isFalse();
		}

		@Test
		void 정책_값을_바꾸면_코드_수정_없이_기준이_바뀐다_AC5() {
			settings.put("NORMAL_DAYS", "14");
			settings.put("NORMAL_MAX_HOURS", "6");

			AppliedPolicy policy = policyService.currentPolicy();

			assertThat(policy.reservableUntil()).isEqualTo(TODAY.plusDays(14));
			assertThat(policy.maxHours()).isEqualTo(6);
		}

		@Test
		void 정책_값이_없으면_설정_오류() {
			settings.remove("NORMAL_DAYS");

			assertThatThrownBy(() -> policyService.currentPolicy()).isInstanceOf(IllegalStateException.class);
		}

	}

	@Nested
	@DisplayName("예약 요청 검증 (US-07 AC2)")
	class ValidateReservation {

		@Test
		void 평상시_7일째_4시간은_허용() {
			LocalDate day = TODAY.plusDays(7);
			assertThatCode(() -> policyService.validateReservation(at(day, 9), at(day, 13))).doesNotThrowAnyException();
		}

		@Test
		void 평상시_8일째는_거부() {
			LocalDate day = TODAY.plusDays(8);
			assertThatThrownBy(() -> policyService.validateReservation(at(day, 9), at(day, 10)))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESERVATION_DATE_OUT_OF_POLICY)
				.hasMessageContaining("평상시")
				.hasMessageContaining(TODAY.plusDays(7).toString());
		}

		@Test
		void 지난_날짜는_거부() {
			LocalDate day = TODAY.minusDays(1);
			assertThatThrownBy(() -> policyService.validateReservation(at(day, 9), at(day, 10)))
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESERVATION_DATE_OUT_OF_POLICY);
		}

		@Test
		void 평상시_5시간은_거부() {
			assertThatThrownBy(() -> policyService.validateReservation(at(TODAY, 12), at(TODAY, 17)))
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESERVATION_DURATION_EXCEEDED)
				.hasMessageContaining("4시간");
		}

		@Test
		void 시험_기간_다음날_3시간은_허용_4시간은_거부() {
			examDays(Set.of(TODAY, TODAY.plusDays(1)));
			LocalDate tomorrow = TODAY.plusDays(1);

			assertThatCode(() -> policyService.validateReservation(at(tomorrow, 9), at(tomorrow, 12)))
				.doesNotThrowAnyException();
			assertThatThrownBy(() -> policyService.validateReservation(at(tomorrow, 9), at(tomorrow, 13)))
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESERVATION_DURATION_EXCEEDED)
				.hasMessageContaining("시험 기간")
				.hasMessageContaining("3시간");
		}

		@Test
		void 시험_기간_모레는_거부() {
			examDays(Set.of(TODAY, TODAY.plusDays(2)));
			LocalDate day = TODAY.plusDays(2);

			assertThatThrownBy(() -> policyService.validateReservation(at(day, 9), at(day, 10)))
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESERVATION_DATE_OUT_OF_POLICY)
				.hasMessageContaining("시험 기간");
		}

	}

	@Nested
	@DisplayName("정책 파라미터 변경 (US-07 AC5)")
	class UpdateSetting {

		@Test
		void 값과_변경_시각이_바뀐다() {
			PolicySetting setting = new PolicySetting("EXAM_MAX_HOURS", "3", null);
			given(policySettingRepository.findById("EXAM_MAX_HOURS")).willReturn(Optional.of(setting));

			policyService.updateSetting("EXAM_MAX_HOURS", " 2 ");

			assertThat(setting.getPolicyValue()).isEqualTo("2");
			assertThat(setting.getUpdatedAt()).isEqualTo(LocalDateTime.now(CLOCK));
		}

		@Test
		void 없는_키는_404() {
			assertThatThrownBy(() -> policyService.updateSetting("UNKNOWN", "1"))
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.POLICY_NOT_FOUND);
		}

		@Test
		void 정수가_아니거나_최솟값_미만이면_400() {
			given(policySettingRepository.findById("NORMAL_MAX_HOURS"))
				.willReturn(Optional.of(new PolicySetting("NORMAL_MAX_HOURS", "4", null)));

			assertThatThrownBy(() -> policyService.updateSetting("NORMAL_MAX_HOURS", "abc"))
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_POLICY_VALUE);
			assertThatThrownBy(() -> policyService.updateSetting("NORMAL_MAX_HOURS", "0"))
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_POLICY_VALUE);
		}

		@Test
		void 기간_값은_0_허용() {
			PolicySetting setting = new PolicySetting("EXAM_DAYS", "1", null);
			given(policySettingRepository.findById("EXAM_DAYS")).willReturn(Optional.of(setting));

			policyService.updateSetting("EXAM_DAYS", "0");

			assertThat(setting.getPolicyValue()).isEqualTo("0");
		}

	}

}
