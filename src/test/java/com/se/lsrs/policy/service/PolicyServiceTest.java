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
import com.se.lsrs.support.DemoLog;

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
				"CHECKIN_WINDOW_MIN", "15", "OPEN_HOUR", "5", "CLOSE_HOUR", "24"));
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
			DemoLog.allowed("E2 평상시 +7일(12-08)에 4시간 예약 → 경계 안쪽 허용");
		}

		@Test
		void 평상시_8일째는_거부() {
			LocalDate day = TODAY.plusDays(8);
			assertThatThrownBy(() -> policyService.validateReservation(at(day, 9), at(day, 10)))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESERVATION_DATE_OUT_OF_POLICY)
				.hasMessageContaining("평상시")
				.hasMessageContaining(TODAY.plusDays(7).toString())
				.satisfies(e -> DemoLog.rejected("E2 평상시 +8일(12-09) 예약 → 경계 바깥 거부", e));
		}

		@Test
		void 지난_날짜는_거부() {
			LocalDate day = TODAY.minusDays(1);
			assertThatThrownBy(() -> policyService.validateReservation(at(day, 9), at(day, 10)))
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESERVATION_DATE_OUT_OF_POLICY)
				.satisfies(e -> DemoLog.rejected("E2 어제 날짜 예약 → 거부", e));
		}

		@Test
		void 평상시_5시간은_거부() {
			assertThatThrownBy(() -> policyService.validateReservation(at(TODAY, 12), at(TODAY, 17)))
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESERVATION_DURATION_EXCEEDED)
				.hasMessageContaining("4시간")
				.satisfies(e -> DemoLog.rejected("E2 평상시 5시간 예약(12~17시) → 경계 바깥 거부", e));
		}

		@Test
		void 시험_기간_다음날_3시간은_허용_4시간은_거부() {
			examDays(Set.of(TODAY, TODAY.plusDays(1)));
			LocalDate tomorrow = TODAY.plusDays(1);

			assertThatCode(() -> policyService.validateReservation(at(tomorrow, 9), at(tomorrow, 12)))
				.doesNotThrowAnyException();
			DemoLog.allowed("E2 시험 기간 다음 날 3시간 예약 → 경계 안쪽 허용");
			assertThatThrownBy(() -> policyService.validateReservation(at(tomorrow, 9), at(tomorrow, 13)))
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESERVATION_DURATION_EXCEEDED)
				.hasMessageContaining("시험 기간")
				.hasMessageContaining("3시간")
				.satisfies(e -> DemoLog.rejected("E2 시험 기간 다음 날 4시간 예약 → 경계 바깥 거부", e));
		}

		@Test
		void 시험_기간_모레는_거부() {
			examDays(Set.of(TODAY, TODAY.plusDays(2)));
			LocalDate day = TODAY.plusDays(2);

			assertThatThrownBy(() -> policyService.validateReservation(at(day, 9), at(day, 10)))
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESERVATION_DATE_OUT_OF_POLICY)
				.hasMessageContaining("시험 기간")
				.satisfies(e -> DemoLog.rejected("E2 시험 기간에 모레 예약 → 거부", e));
		}

	}

	@Nested
	@DisplayName("시간대 검증: 정시 1시간 단위·운영 시간 05~24시 (US-05·06)")
	class TimeSlot {

		@Test
		void 운영_시간_양_끝까지_허용한다() {
			assertThatCode(() -> policyService.validateTimeSlot(at(TODAY, 5), at(TODAY, 7))).doesNotThrowAnyException();
			assertThatCode(() -> policyService.validateTimeSlot(at(TODAY, 22), TODAY.plusDays(1).atStartOfDay()))
				.doesNotThrowAnyException();
			DemoLog.allowed("E2 운영 시간 양 끝(05~07시, 22~24시) 예약 → 경계 안쪽 허용");
		}

		@Test
		void 운영_시간_밖이면_거부한다() {
			assertThatThrownBy(() -> policyService.validateTimeSlot(at(TODAY, 4), at(TODAY, 6)))
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_RESERVATION_TIME)
				.hasMessageContaining("05:00~24:00")
				.satisfies(e -> DemoLog.rejected("E2 04~06시 예약 → 운영 시간 밖 거부", e));
			assertThatThrownBy(() -> policyService.validateTimeSlot(at(TODAY, 23), TODAY.plusDays(1).atTime(1, 0)))
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_RESERVATION_TIME)
				.satisfies(e -> DemoLog.rejected("E2 23~01시 예약 → 운영 시간 밖 거부", e));
		}

		@Test
		void 정시가_아니면_거부한다() {
			assertThatThrownBy(() -> policyService.validateTimeSlot(TODAY.atTime(13, 30), at(TODAY, 15)))
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_RESERVATION_TIME)
				.hasMessageContaining("정시")
				.satisfies(e -> DemoLog.rejected("E2 13:30 시작 예약 → 정시 아님 거부", e));
		}

		@Test
		void 종료가_시작보다_늦지_않으면_거부한다() {
			assertThatThrownBy(() -> policyService.validateTimeSlot(at(TODAY, 15), at(TODAY, 15)))
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_RESERVATION_TIME)
				.satisfies(e -> DemoLog.rejected("E2 15~15시 예약 → 종료 = 시작 거부", e));
			assertThatThrownBy(() -> policyService.validateTimeSlot(at(TODAY, 15), at(TODAY, 13)))
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_RESERVATION_TIME)
				.satisfies(e -> DemoLog.rejected("E2 15~13시 예약 → 종료 < 시작 거부", e));
		}

		@Test
		void 예약_검증은_시간대도_검사한다() {
			assertThatThrownBy(() -> policyService.validateReservation(at(TODAY, 3), at(TODAY, 5)))
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_RESERVATION_TIME);
		}

		@Test
		void 운영_시간_값을_바꾸면_기준이_바뀐다() {
			settings.put("OPEN_HOUR", "9");

			assertThat(policyService.operatingHours()).isEqualTo(new OperatingHours(9, 24));
			assertThatThrownBy(() -> policyService.validateTimeSlot(at(TODAY, 8), at(TODAY, 10)))
				.hasMessageContaining("09:00~24:00");
		}

		@Test
		void 예약_가능_기간_밖의_날짜는_조회도_거부한다_US05_AC4() {
			assertThat(policyService.validateReservableDate(TODAY.plusDays(7)).maxHours()).isEqualTo(4);
			DemoLog.allowed("E2 좌석 조회 +7일 → 경계 안쪽 허용");
			assertThatThrownBy(() -> policyService.validateReservableDate(TODAY.plusDays(8)))
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESERVATION_DATE_OUT_OF_POLICY)
				.satisfies(e -> DemoLog.rejected("E2 좌석 조회 +8일 → 경계 바깥 거부", e));
			assertThatThrownBy(() -> policyService.validateReservableDate(TODAY.minusDays(1)))
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESERVATION_DATE_OUT_OF_POLICY)
				.satisfies(e -> DemoLog.rejected("E2 좌석 조회 어제 → 거부", e));
		}

	}

	@Nested
	@DisplayName("정책 파라미터 변경 (US-07 AC5)")
	class UpdateSetting {

		@Test
		void 운영_시간은_범위_밖이면_400() {
			given(policySettingRepository.findById("CLOSE_HOUR"))
				.willReturn(Optional.of(new PolicySetting("CLOSE_HOUR", "24", null)));

			assertThatThrownBy(() -> policyService.updateSetting("CLOSE_HOUR", "25"))
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_POLICY_VALUE)
				.hasMessageContaining("1~24")
				.satisfies(e -> DemoLog.rejected("E2 마감 시각을 25시로 변경 → 거부", e));
		}

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
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_POLICY_VALUE)
				.satisfies(e -> DemoLog.rejected("E2 최대 시간을 abc로 변경 → 거부", e));
			assertThatThrownBy(() -> policyService.updateSetting("NORMAL_MAX_HOURS", "0"))
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_POLICY_VALUE)
				.satisfies(e -> DemoLog.rejected("E2 최대 시간을 0으로 변경 → 최솟값 미만 거부", e));
		}

		@Test
		void 기간_값은_0_허용() {
			PolicySetting setting = new PolicySetting("EXAM_DAYS", "1", null);
			given(policySettingRepository.findById("EXAM_DAYS")).willReturn(Optional.of(setting));

			policyService.updateSetting("EXAM_DAYS", "0");

			assertThat(setting.getPolicyValue()).isEqualTo("0");
			DemoLog.allowed("E2 기간 값을 0일로 변경 → 경계 안쪽 허용");
		}

	}

}
