package com.se.lsrs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.se.lsrs.common.auth.CurrentUserProvider;

/**
 * US-07·08 전 계층 통합 테스트 (MySQL Testcontainers). 인증(US-02) 대신 CurrentUserProvider 를 대체한다.
 * 현재 시각은 2026-12-01 10:00 으로 고정한다. 데이터: fixture-reservations.sql
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import({ TestcontainersConfiguration.class, Us07Us08IntegrationTest.FixedClockConfig.class })
@Testcontainers(disabledWithoutDocker = true)
@Transactional
@Sql("/fixture-reservations.sql")
class Us07Us08IntegrationTest {

	private static final ZoneId ZONE = ZoneId.systemDefault();

	@TestConfiguration(proxyBeanMethods = false)
	static class FixedClockConfig {

		@Bean
		@Primary
		Clock fixedClock() {
			return Clock.fixed(LocalDateTime.of(2026, 12, 1, 10, 0).atZone(ZONE).toInstant(), ZONE);
		}

	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@MockitoBean
	private CurrentUserProvider currentUserProvider;

	@BeforeEach
	void loginAsStudent1() {
		given(currentUserProvider.currentUserId()).willReturn(1L);
	}

	private static String json(String body) {
		return body.replace('\'', '"');
	}

	@Test
	void US08_내_예약_조회_필터_직접_취소() throws Exception {
		// AC1: 본인 예약만 최신순, 취소 가능 여부
		mockMvc.perform(get("/api/reservations/me"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(4))
			.andExpect(jsonPath("$[*].reservationId").value(org.hamcrest.Matchers.contains(11, 10, 12, 14)))
			.andExpect(jsonPath("$[0].seatNo").value("A-02"))
			.andExpect(jsonPath("$[0].cancellable").value(true))
			.andExpect(jsonPath("$[1].cancellable").value(false));

		// AC2: 상태 필터
		mockMvc.perform(get("/api/reservations/me").param("status", "CANCELLED"))
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].reservationId").value(12));

		// AC3: 사유 선택 취소
		mockMvc.perform(put("/api/reservations/11/cancel").contentType(MediaType.APPLICATION_JSON)
			.content(json("{'reason':'TIME_CHANGE'}")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("CANCELLED"))
			.andExpect(jsonPath("$.cancelReason").value("TIME_CHANGE"));

		// AC4: 노쇼 카운터 불변, 진행 중 예약에서 빠짐(= 좌석 즉시 가용), 취소 주체 STUDENT
		assertThat(jdbcTemplate.queryForObject("select no_show_count from users where user_id = 1", Integer.class))
			.isZero();
		assertThat(jdbcTemplate.queryForObject(
				"select count(*) from reservation where seat_id = 2 and status in ('RESERVED','CHECKED_IN') and start_at < '2026-12-02 15:00:00' and end_at > '2026-12-02 13:00:00'",
				Integer.class))
			.isZero();
		assertThat(jdbcTemplate.queryForObject("select canceled_by from reservation where reservation_id = 11",
				String.class))
			.isEqualTo("STUDENT");

		// AC5: 다시 취소·종결 상태·타인 예약은 거부
		mockMvc.perform(put("/api/reservations/11/cancel").contentType(MediaType.APPLICATION_JSON)
			.content(json("{'reason':'OTHER'}")))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.message").value("이미 취소된 예약입니다."));
		mockMvc.perform(put("/api/reservations/10/cancel").contentType(MediaType.APPLICATION_JSON)
			.content(json("{'reason':'OTHER'}")))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.message").value("이미 체크인한 예약은 취소할 수 없습니다."));
		mockMvc.perform(put("/api/reservations/13/cancel").contentType(MediaType.APPLICATION_JSON)
			.content(json("{'reason':'OTHER'}")))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("RESERVATION_NOT_FOUND"));
		assertThat(jdbcTemplate.queryForObject("select status from reservation where reservation_id = 13",
				String.class))
			.isEqualTo("RESERVED");
	}

	@Test
	void US07_시험_기간_설정과_정책_적용() throws Exception {
		// 평상시: 오늘 ~ +7일, 4시간
		mockMvc.perform(get("/api/policies/current"))
			.andExpect(jsonPath("$.examPeriod").value(false))
			.andExpect(jsonPath("$.reservableUntil").value("2026-12-08"))
			.andExpect(jsonPath("$.maxHours").value(4));

		// 대상일이 시험 기간(12-14~20)이면 시험 정책
		mockMvc.perform(get("/api/policies/current").param("date", "2026-12-14"))
			.andExpect(jsonPath("$.examPeriod").value(true))
			.andExpect(jsonPath("$.reservableUntil").value("2026-12-02"))
			.andExpect(jsonPath("$.maxHours").value(3));

		// AC4: 잘못된 기간·겹치는 기간 거부
		mockMvc.perform(post("/api/admin/exam-periods").contentType(MediaType.APPLICATION_JSON)
			.content(json("{'startDate':'2026-12-25','endDate':'2026-12-20'}")))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_EXAM_PERIOD"));
		mockMvc.perform(post("/api/admin/exam-periods").contentType(MediaType.APPLICATION_JSON)
			.content(json("{'startDate':'2026-12-18','endDate':'2026-12-22'}")))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("EXAM_PERIOD_OVERLAP"));

		// AC1·3: 오늘을 포함하는 시험 기간 등록 → 오늘부터 시험 정책
		mockMvc.perform(post("/api/admin/exam-periods").contentType(MediaType.APPLICATION_JSON)
			.content(json("{'startDate':'2026-12-01','endDate':'2026-12-03'}")))
			.andExpect(status().isCreated());
		mockMvc.perform(get("/api/admin/exam-periods")).andExpect(jsonPath("$.length()").value(2));
		mockMvc.perform(get("/api/policies/current"))
			.andExpect(jsonPath("$.examPeriod").value(true))
			.andExpect(jsonPath("$.reservableUntil").value("2026-12-02"))
			.andExpect(jsonPath("$.maxHours").value(3));

		// AC5: DB 파라미터만 바꾸면 코드 수정 없이 기준이 바뀐다
		mockMvc.perform(put("/api/admin/policies/EXAM_MAX_HOURS").contentType(MediaType.APPLICATION_JSON)
			.content(json("{'policyValue':'2'}")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.policyValue").value("2"));
		mockMvc.perform(get("/api/policies/current")).andExpect(jsonPath("$.maxHours").value(2));
		mockMvc.perform(put("/api/admin/policies/EXAM_MAX_HOURS").contentType(MediaType.APPLICATION_JSON)
			.content(json("{'policyValue':'0'}")))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_POLICY_VALUE"));
		mockMvc.perform(put("/api/admin/policies/NOPE").contentType(MediaType.APPLICATION_JSON)
			.content(json("{'policyValue':'1'}")))
			.andExpect(status().isNotFound());
	}

}
