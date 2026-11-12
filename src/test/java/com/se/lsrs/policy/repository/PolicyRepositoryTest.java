package com.se.lsrs.policy.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.se.lsrs.TestcontainersConfiguration;
import com.se.lsrs.policy.domain.PolicySetting;

/** MySQL 에서 시험 기간 조회 쿼리와 정책 기본값(data.sql)을 검증한다 (US-07). */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
@Testcontainers(disabledWithoutDocker = true)
@Sql("/fixture-reservations.sql")
class PolicyRepositoryTest {

	@Autowired
	private ExamPeriodRepository examPeriodRepository;

	@Autowired
	private PolicySettingRepository policySettingRepository;

	@Test
	void 시험_기간_포함_여부는_양_끝을_포함한다() {
		assertThat(examPeriodRepository.existsCovering(LocalDate.of(2026, 12, 13))).isFalse();
		assertThat(examPeriodRepository.existsCovering(LocalDate.of(2026, 12, 14))).isTrue();
		assertThat(examPeriodRepository.existsCovering(LocalDate.of(2026, 12, 20))).isTrue();
		assertThat(examPeriodRepository.existsCovering(LocalDate.of(2026, 12, 21))).isFalse();
	}

	@Test
	void 겹치는_기간을_찾는다() {
		assertThat(examPeriodRepository.existsOverlapping(LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 14)))
			.isTrue();
		assertThat(examPeriodRepository.existsOverlapping(LocalDate.of(2026, 12, 15), LocalDate.of(2026, 12, 16)))
			.isTrue();
		assertThat(examPeriodRepository.existsOverlapping(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31)))
			.isTrue();
		assertThat(examPeriodRepository.existsOverlapping(LocalDate.of(2026, 12, 21), LocalDate.of(2026, 12, 25)))
			.isFalse();
	}

	@Test
	void 정책_기본값이_DB에_있다() {
		assertThat(policySettingRepository.findAllByOrderByPolicyKeyAsc()).extracting(PolicySetting::getPolicyKey)
			.contains("NORMAL_DAYS", "NORMAL_MAX_HOURS", "EXAM_DAYS", "EXAM_MAX_HOURS", "CHECKIN_WINDOW_MIN",
					"OPEN_HOUR", "CLOSE_HOUR");
		assertThat(policySettingRepository.findById("OPEN_HOUR").orElseThrow().getPolicyValue()).isEqualTo("5");
		assertThat(policySettingRepository.findById("CLOSE_HOUR").orElseThrow().getPolicyValue()).isEqualTo("24");
		assertThat(policySettingRepository.findById("NORMAL_DAYS").orElseThrow().getPolicyValue()).isEqualTo("7");
		assertThat(policySettingRepository.findById("EXAM_MAX_HOURS").orElseThrow().getPolicyValue()).isEqualTo("3");
	}

}
