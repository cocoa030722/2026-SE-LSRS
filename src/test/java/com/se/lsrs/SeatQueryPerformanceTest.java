package com.se.lsrs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.se.lsrs.common.auth.CurrentUserProvider;

/**
 * 좌석 조회 API 응답 시간 500ms 이내 (US-05 AC5, 설계서 1장: 좌석 200석·예약 1만 건).
 * MySQL Testcontainers 에서 GET /api/seats 전 계층(MockMvc)을 측정한다. 워밍업 후 10회 모두 500ms 이내여야 한다.
 * 현재 시각은 2026-12-01 10:00 으로 고정한다. 데이터: fixture-performance.sql
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import({ TestcontainersConfiguration.class, SeatQueryPerformanceTest.FixedClockConfig.class })
@Testcontainers(disabledWithoutDocker = true)
@Transactional
@Sql("/fixture-performance.sql")
class SeatQueryPerformanceTest {

	private static final ZoneId ZONE = ZoneId.systemDefault();

	private static final Duration LIMIT = Duration.ofMillis(500);

	private static final int WARM_UP = 3;

	private static final int RUNS = 10;

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

	private ResultActions seats() throws Exception {
		return mockMvc.perform(get("/api/seats").param("date", "2026-12-02").param("start", "09:00").param("end", "15:00"));
	}

	@Test
	void 좌석_200석_예약_1만건에서_좌석_조회가_500ms_이내다_AC5() throws Exception {
		// 학생 1의 예약(i%100=0)은 모두 CANCELLED 이므로 진행 중 예약이 있는 학생 2로 조회한다
		given(currentUserProvider.currentUserId()).willReturn(2L);
		assertThat(jdbcTemplate.queryForObject("select count(*) from seat", Integer.class)).isEqualTo(200);
		assertThat(jdbcTemplate.queryForObject("select count(*) from reservation", Integer.class)).isEqualTo(10_000);

		// 결과 검증: 200석 전체, 상태 계산이 실제로 일어난다
		seats().andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(200))
			.andExpect(jsonPath("$[?(@.status == 'MINE')]").isNotEmpty())
			.andExpect(jsonPath("$[?(@.status == 'RESERVED')]").isNotEmpty())
			.andExpect(jsonPath("$[?(@.status == 'AVAILABLE')]").isNotEmpty());

		for (int i = 0; i < WARM_UP; i++) {
			seats().andExpect(status().isOk());
		}
		List<Duration> elapsed = new ArrayList<>();
		for (int i = 0; i < RUNS; i++) {
			long begin = System.nanoTime();
			seats().andExpect(status().isOk());
			elapsed.add(Duration.ofNanos(System.nanoTime() - begin));
		}

		Duration max = Collections.max(elapsed);
		System.out.printf("[US-05 AC5] GET /api/seats (200석·1만 건) %d회: 최대 %dms, 평균 %dms%n", RUNS, max.toMillis(),
				elapsed.stream().mapToLong(Duration::toMillis).sum() / RUNS);
		assertThat(max).isLessThan(LIMIT);
	}

}
