package com.se.lsrs.reservation.controller;

import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.se.lsrs.common.error.BusinessException;
import com.se.lsrs.common.error.ErrorCode;
import com.se.lsrs.reservation.domain.CancelReason;
import com.se.lsrs.reservation.domain.ReservationStatus;
import com.se.lsrs.reservation.dto.MyReservationResponse;
import com.se.lsrs.reservation.dto.ReservationResponse;
import com.se.lsrs.reservation.service.ReservationService;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

	private static final LocalDateTime START = LocalDateTime.of(2026, 12, 1, 13, 0);

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ReservationService reservationService;

	private static MyReservationResponse item(long id, ReservationStatus status, CancelReason reason,
			boolean cancellable) {
		return new MyReservationResponse(id, "A-01", START, START.plusHours(2), status, reason, cancellable);
	}

	@Test
	void 예약을_생성하면_201과_좌석_시간_정보_US06_AC1() throws Exception {
		given(reservationService.reserve(3L, START, START.plusHours(2))).willReturn(new ReservationResponse(100L, 3L,
				"A-03", START, START.plusHours(2), ReservationStatus.RESERVED, START.minusHours(3)));

		mockMvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON)
			.content("{\"seatId\":3,\"startAt\":\"2026-12-01T13:00:00\",\"endAt\":\"2026-12-01T15:00:00\"}"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.reservationId").value(100))
			.andExpect(jsonPath("$.seatNo").value("A-03"))
			.andExpect(jsonPath("$.startAt").value("2026-12-01T13:00:00"))
			.andExpect(jsonPath("$.endAt").value("2026-12-01T15:00:00"))
			.andExpect(jsonPath("$.status").value("RESERVED"));
	}

	@Test
	void 좌석이나_시간대를_선택하지_않으면_400_US06_AC2() throws Exception {
		mockMvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON)
			.content("{\"startAt\":\"2026-12-01T13:00:00\",\"endAt\":\"2026-12-01T15:00:00\"}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_INPUT"))
			.andExpect(jsonPath("$.message").value("좌석을 선택해 주세요."));
		mockMvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON).content("{\"seatId\":3}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_INPUT"));
	}

	@Test
	void 좌석_충돌과_본인_중복은_서로_다른_코드의_409_US06_AC2_AC4() throws Exception {
		given(reservationService.reserve(3L, START, START.plusHours(2)))
			.willThrow(new BusinessException(ErrorCode.SEAT_ALREADY_RESERVED));
		given(reservationService.reserve(4L, START, START.plusHours(2)))
			.willThrow(new BusinessException(ErrorCode.DUPLICATE_RESERVATION));

		mockMvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON)
			.content("{\"seatId\":3,\"startAt\":\"2026-12-01T13:00:00\",\"endAt\":\"2026-12-01T15:00:00\"}"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("SEAT_ALREADY_RESERVED"))
			.andExpect(jsonPath("$.message").value("이미 예약된 좌석입니다."));
		mockMvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON)
			.content("{\"seatId\":4,\"startAt\":\"2026-12-01T13:00:00\",\"endAt\":\"2026-12-01T15:00:00\"}"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("DUPLICATE_RESERVATION"));
	}

	@Test
	void 이용_제한_학생은_403_US06_AC3() throws Exception {
		given(reservationService.reserve(3L, START, START.plusHours(2))).willThrow(
				new BusinessException(ErrorCode.USER_RESTRICTED, "이용 제한 중입니다. 2026-12-08 10:00 이후에 예약할 수 있습니다."));

		mockMvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON)
			.content("{\"seatId\":3,\"startAt\":\"2026-12-01T13:00:00\",\"endAt\":\"2026-12-01T15:00:00\"}"))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("USER_RESTRICTED"))
			.andExpect(jsonPath("$.message").value("이용 제한 중입니다. 2026-12-08 10:00 이후에 예약할 수 있습니다."));
	}

	@Test
	void 내_예약_전체를_조회한다_AC1() throws Exception {
		given(reservationService.getMyReservations(isNull()))
			.willReturn(List.of(item(2, ReservationStatus.RESERVED, null, true),
					item(1, ReservationStatus.CANCELLED, CancelReason.TIME_CHANGE, false)));

		mockMvc.perform(get("/api/reservations/me"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].reservationId").value(2))
			.andExpect(jsonPath("$[0].seatNo").value("A-01"))
			.andExpect(jsonPath("$[0].startAt").value("2026-12-01T13:00:00"))
			.andExpect(jsonPath("$[0].status").value("RESERVED"))
			.andExpect(jsonPath("$[0].cancellable").value(true))
			.andExpect(jsonPath("$[1].cancelReason").value("TIME_CHANGE"));
	}

	@Test
	void 상태로_필터링한다_AC2() throws Exception {
		given(reservationService.getMyReservations(ReservationStatus.AUTO_CANCELLED))
			.willReturn(List.of(item(1, ReservationStatus.AUTO_CANCELLED, null, false)));

		mockMvc.perform(get("/api/reservations/me").param("status", "AUTO_CANCELLED"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].status").value("AUTO_CANCELLED"));
	}

	@Test
	void 없는_상태값은_400() throws Exception {
		mockMvc.perform(get("/api/reservations/me").param("status", "NOPE"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_INPUT"));
	}

	@Test
	void 사유를_선택해_취소한다_AC3() throws Exception {
		given(reservationService.cancelByStudent(1L, CancelReason.SEAT_CHANGE))
			.willReturn(item(1, ReservationStatus.CANCELLED, CancelReason.SEAT_CHANGE, false));

		mockMvc.perform(put("/api/reservations/1/cancel").contentType(MediaType.APPLICATION_JSON)
			.content("{\"reason\":\"SEAT_CHANGE\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("CANCELLED"))
			.andExpect(jsonPath("$.cancelReason").value("SEAT_CHANGE"));
	}

	@Test
	void 사유가_없으면_400() throws Exception {
		mockMvc.perform(put("/api/reservations/1/cancel").contentType(MediaType.APPLICATION_JSON).content("{}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_INPUT"))
			.andExpect(jsonPath("$.message").value("취소 사유를 선택해 주세요."));
	}

	@Test
	void 취소할_수_없는_예약은_409_AC5() throws Exception {
		given(reservationService.cancelByStudent(1L, CancelReason.OTHER))
			.willThrow(new BusinessException(ErrorCode.RESERVATION_NOT_CANCELLABLE, "이미 취소된 예약입니다."));

		mockMvc.perform(put("/api/reservations/1/cancel").contentType(MediaType.APPLICATION_JSON)
			.content("{\"reason\":\"OTHER\"}"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("RESERVATION_NOT_CANCELLABLE"))
			.andExpect(jsonPath("$.message").value("이미 취소된 예약입니다."));
	}

	@Test
	void 다른_학생의_예약은_404_AC5() throws Exception {
		given(reservationService.cancelByStudent(99L, CancelReason.OTHER))
			.willThrow(new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));

		mockMvc.perform(put("/api/reservations/99/cancel").contentType(MediaType.APPLICATION_JSON)
			.content("{\"reason\":\"OTHER\"}"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("RESERVATION_NOT_FOUND"));
	}

	@Test
	void 미인증이면_401() throws Exception {
		given(reservationService.getMyReservations(isNull()))
			.willThrow(new BusinessException(ErrorCode.UNAUTHENTICATED));

		mockMvc.perform(get("/api/reservations/me"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
	}

}
