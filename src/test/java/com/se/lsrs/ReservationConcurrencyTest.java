package com.se.lsrs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.se.lsrs.common.auth.CurrentUserProvider;
import com.se.lsrs.common.error.BusinessException;
import com.se.lsrs.reservation.service.ReservationService;

/**
 * 동시 예약 20건에서 이중 배정 0건 (US-06 AC4, 설계서 1장 성공 기준). 실제 MySQL 잠금을 검증하므로
 * 테스트 트랜잭션 롤백을 쓰지 않고 각 요청이 자기 트랜잭션으로 커밋되게 한 뒤 cleanup.sql 로 정리한다.
 */
@SpringBootTest
@Import({ TestcontainersConfiguration.class, ReservationConcurrencyTest.FixedClockConfig.class })
@Testcontainers(disabledWithoutDocker = true)
@Sql({ "/cleanup.sql", "/fixture-concurrency.sql" })
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class ReservationConcurrencyTest {

	private static final ZoneId ZONE = ZoneId.systemDefault();

	private static final int REQUESTS = 20;

	private static final LocalDateTime START = LocalDateTime.of(2026, 12, 1, 13, 0);

	private static final LocalDateTime END = START.plusHours(2);

	/** 요청 스레드별 로그인 학생 */
	private static final ThreadLocal<Long> LOGIN = new ThreadLocal<>();

	@TestConfiguration(proxyBeanMethods = false)
	static class FixedClockConfig {

		@Bean
		@Primary
		Clock fixedClock() {
			return Clock.fixed(LocalDateTime.of(2026, 12, 1, 10, 0).atZone(ZONE).toInstant(), ZONE);
		}

	}

	@Autowired
	private ReservationService reservationService;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@MockitoBean
	private CurrentUserProvider currentUserProvider;

	@BeforeEach
	void setUp() {
		given(currentUserProvider.currentUserId()).willAnswer(invocation -> LOGIN.get());
	}

	/** 요청 i(1~20)를 동시에 출발시키고 결과(성공 "OK" 또는 오류 코드)별 건수를 센다. */
	private Map<String, Long> runConcurrently(Function<Integer, Long> userOf, Function<Integer, Long> seatOf)
			throws Exception {
		CountDownLatch ready = new CountDownLatch(REQUESTS);
		CountDownLatch go = new CountDownLatch(1);
		List<Future<String>> results = new ArrayList<>();
		try (ExecutorService executor = Executors.newFixedThreadPool(REQUESTS)) {
			for (int i = 1; i <= REQUESTS; i++) {
				int request = i;
				Callable<String> task = () -> {
					LOGIN.set(userOf.apply(request));
					ready.countDown();
					go.await();
					try {
						reservationService.reserve(seatOf.apply(request), START, END);
						return "OK";
					}
					catch (BusinessException e) {
						return e.getErrorCode().name();
					}
					finally {
						LOGIN.remove();
					}
				};
				results.add(executor.submit(task));
			}
			ready.await();
			go.countDown();
			List<String> outcomes = new ArrayList<>();
			for (Future<String> result : results) {
				outcomes.add(result.get());
			}
			return outcomes.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
		}
	}

	private int activeReservations(String where) {
		return jdbcTemplate.queryForObject(
				"select count(*) from reservation where status in ('RESERVED','CHECKED_IN') and " + where,
				Integer.class);
	}

	@Test
	void 학생_20명이_같은_좌석_같은_시간대를_동시에_예약하면_1건만_생성된다_AC4() throws Exception {
		Map<String, Long> outcomes = runConcurrently(request -> (long) request, request -> 1L);

		assertThat(outcomes).containsEntry("OK", 1L).containsEntry("SEAT_ALREADY_RESERVED", 19L).hasSize(2);
		assertThat(activeReservations("seat_id = 1")).isEqualTo(1);
	}

	@Test
	void 한_학생이_같은_시간대에_좌석_20개를_동시에_예약하면_1건만_생성된다_FR_R_11() throws Exception {
		Map<String, Long> outcomes = runConcurrently(request -> 1L, request -> (long) request);

		assertThat(outcomes).containsEntry("OK", 1L).containsEntry("DUPLICATE_RESERVATION", 19L).hasSize(2);
		assertThat(activeReservations("user_id = 1")).isEqualTo(1);
	}

}
