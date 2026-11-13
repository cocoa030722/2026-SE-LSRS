package com.se.lsrs.seat.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.se.lsrs.common.error.BusinessException;
import com.se.lsrs.common.error.ErrorCode;
import com.se.lsrs.seat.domain.SeatStatus;
import com.se.lsrs.seat.dto.SeatStatusResponse;
import com.se.lsrs.seat.service.SeatQueryService;

@WebMvcTest(SeatController.class)
class SeatControllerTest {

	private static final LocalDate DATE = LocalDate.of(2026, 12, 1);

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private SeatQueryService seatQueryService;

	@Test
	void 날짜와_시간대로_좌석_그리드를_조회한다_AC1_AC2() throws Exception {
		given(seatQueryService.getSeats(DATE, "13:00", "15:00"))
			.willReturn(List.of(new SeatStatusResponse(1L, "A-01", "A", 1, 1, SeatStatus.MINE),
					new SeatStatusResponse(2L, "A-02", "A", 1, 2, SeatStatus.AVAILABLE)));

		mockMvc.perform(get("/api/seats").param("date", "2026-12-01").param("start", "13:00").param("end", "15:00"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].seatNo").value("A-01"))
			.andExpect(jsonPath("$[0].gridRow").value(1))
			.andExpect(jsonPath("$[0].gridCol").value(1))
			.andExpect(jsonPath("$[0].status").value("MINE"))
			.andExpect(jsonPath("$[1].status").value("AVAILABLE"));
	}

	@Test
	void 파라미터가_없거나_날짜_형식이_틀리면_400() throws Exception {
		mockMvc.perform(get("/api/seats").param("date", "2026-12-01").param("start", "13:00"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_INPUT"));
		mockMvc.perform(get("/api/seats").param("date", "12/01").param("start", "13:00").param("end", "15:00"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_INPUT"));
	}

	@Test
	void 예약_가능_범위_밖의_날짜는_안내와_함께_400_AC4() throws Exception {
		given(seatQueryService.getSeats(LocalDate.of(2026, 12, 20), "13:00", "15:00")).willThrow(new BusinessException(
				ErrorCode.RESERVATION_DATE_OUT_OF_POLICY, "평상시 예약 가능 기간은 2026-12-01 ~ 2026-12-08 입니다."));

		mockMvc.perform(get("/api/seats").param("date", "2026-12-20").param("start", "13:00").param("end", "15:00"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("RESERVATION_DATE_OUT_OF_POLICY"))
			.andExpect(jsonPath("$.message").value("평상시 예약 가능 기간은 2026-12-01 ~ 2026-12-08 입니다."));
	}

}
