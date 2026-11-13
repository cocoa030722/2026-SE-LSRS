package com.se.lsrs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.se.lsrs.common.auth.CurrentUserProvider;

/**
 * US-05·06 전 계층 통합 테스트 (MySQL Testcontainers). 인증(US-02) 대신 CurrentUserProvider 를 대체한다.
 * 현재 시각은 2026-12-01 10:00 으로 고정한다. 데이터: fixture-reservations.sql + 학생 3(이용 제한)·4
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import({ TestcontainersConfiguration.class, Us05Us06IntegrationTest.FixedClockConfig.class })
@Testcontainers(disabledWithoutDocker = true)
@Transactional
@Sql("/fixture-reservations.sql")
class Us05Us06IntegrationTest {

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
	void setUp() {
		jdbcTemplate.update("""
				INSERT INTO users (user_id, login_id, email, password_hash, role, status, no_show_count,
					restricted_until, token_version) VALUES
				  (3, '20260003', 's3@test.ac.kr', 'hash', 'STUDENT', 'ACTIVE', 3, '2026-12-08 10:00:00', 0),
				  (4, '20260004', 's4@test.ac.kr', 'hash', 'STUDENT', 'ACTIVE', 0, NULL, 0)
				""");
		loginAs(1L);
	}

	private void loginAs(long userId) {
		given(currentUserProvider.currentUserId()).willReturn(userId);
	}

	private ResultActions seats(String date, String start, String end) throws Exception {
		return mockMvc.perform(get("/api/seats").param("date", date).param("start", start).param("end", end));
	}

	private ResultActions reserve(long seatId, String startAt, String endAt) throws Exception {
		return mockMvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON)
			.content("{\"seatId\":%d,\"startAt\":\"%s\",\"endAt\":\"%s\"}".formatted(seatId, startAt, endAt)));
	}

	@Test
	void US05_좌석_현황_조회와_상태_구분() throws Exception {
		// AC1·2: 학생 1 기준. 12-02 13~15 에 A-02 는 내 예약(예약 11)
		seats("2026-12-02", "13:00", "15:00").andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2))
			.andExpect(jsonPath("$[0].seatNo").value("A-01"))
			.andExpect(jsonPath("$[0].status").value("AVAILABLE"))
			.andExpect(jsonPath("$[1].status").value("MINE"));
		// 지금(10~11시) A-01 은 학생 1이 체크인한 좌석(예약 10)
		seats("2026-12-01", "10:00", "11:00").andExpect(jsonPath("$[0].status").value("MINE"));

		// 다른 학생에게는 사용 중·예약됨
		loginAs(2L);
		seats("2026-12-01", "10:00", "11:00").andExpect(jsonPath("$[0].status").value("IN_USE"));
		seats("2026-12-02", "13:00", "15:00").andExpect(jsonPath("$[1].status").value("RESERVED"));

		// AC3: 다른 학생이 예약하면 다음 조회에 즉시 반영
		loginAs(4L);
		reserve(1, "2026-12-02T13:00:00", "2026-12-02T14:00:00").andExpect(status().isCreated());
		loginAs(2L);
		seats("2026-12-02", "13:00", "15:00").andExpect(jsonPath("$[0].status").value("RESERVED"));

		// AC4: 예약 가능 범위(평상시 오늘~+7일) 밖은 안내와 함께 거부
		seats("2026-12-09", "13:00", "15:00").andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("RESERVATION_DATE_OUT_OF_POLICY"))
			.andExpect(jsonPath("$.message").value("평상시 예약 가능 기간은 2026-12-01 ~ 2026-12-08 입니다."));
		// 운영 시간(05~24시) 밖·정시 아님
		seats("2026-12-02", "04:00", "06:00").andExpect(jsonPath("$.code").value("INVALID_RESERVATION_TIME"));
		seats("2026-12-02", "22:00", "24:00").andExpect(status().isOk());
	}

	@Test
	void US06_예약_생성과_거부_사유() throws Exception {
		// AC1: RESERVED 로 생성, 좌석·시간 정보 반환
		loginAs(2L);
		reserve(1, "2026-12-02T13:00:00", "2026-12-02T15:00:00").andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value("RESERVED"))
			.andExpect(jsonPath("$.seatNo").value("A-01"))
			.andExpect(jsonPath("$.startAt").value("2026-12-02T13:00:00"))
			.andExpect(jsonPath("$.endAt").value("2026-12-02T15:00:00"));
		assertThat(jdbcTemplate.queryForObject(
				"select count(*) from reservation where user_id = 2 and seat_id = 1 and status = 'RESERVED'",
				Integer.class))
			.isEqualTo(1);

		// AC2: 본인 시간대 중복(다른 좌석이어도) 거부
		reserve(2, "2026-12-02T14:00:00", "2026-12-02T16:00:00").andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("DUPLICATE_RESERVATION"));
		// AC2: 좌석·시간대 미선택 거부
		mockMvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON)
			.content("{\"startAt\":\"2026-12-02T16:00:00\",\"endAt\":\"2026-12-02T17:00:00\"}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("좌석을 선택해 주세요."));

		// AC3: 이용 제한 학생은 해제일 안내와 함께 거부
		loginAs(3L);
		reserve(2, "2026-12-03T13:00:00", "2026-12-03T14:00:00").andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("USER_RESTRICTED"))
			.andExpect(jsonPath("$.message").value("이용 제한 중입니다. 2026-12-08 10:00 이후에 예약할 수 있습니다."));

		// AC4: 이미 예약된 좌석(겹치는 시간대) 거부. 끝과 시작이 맞닿으면 허용
		loginAs(4L);
		reserve(1, "2026-12-02T14:00:00", "2026-12-02T15:00:00").andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("SEAT_ALREADY_RESERVED"))
			.andExpect(jsonPath("$.message").value("이미 예약된 좌석입니다."));
		reserve(1, "2026-12-02T15:00:00", "2026-12-02T16:00:00").andExpect(status().isCreated());

		// 정책: 평상시 최대 4시간, 지난 시간대·운영 시간 밖 거부
		reserve(2, "2026-12-04T09:00:00", "2026-12-04T14:00:00").andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("RESERVATION_DURATION_EXCEEDED"));
		reserve(2, "2026-12-01T09:00:00", "2026-12-01T10:00:00").andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_RESERVATION_TIME"));
		reserve(2, "2026-12-04T23:00:00", "2026-12-05T01:00:00").andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_RESERVATION_TIME"));
		reserve(2, "2026-12-04T22:00:00", "2026-12-05T00:00:00").andExpect(status().isCreated());

		// 거부된 요청은 예약을 남기지 않는다 (AC5)
		assertThat(jdbcTemplate.queryForObject("select count(*) from reservation where user_id in (3, 4)",
				Integer.class))
			.isEqualTo(2);
	}

}
