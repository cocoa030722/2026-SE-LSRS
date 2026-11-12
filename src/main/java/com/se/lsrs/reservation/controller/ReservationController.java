package com.se.lsrs.reservation.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.se.lsrs.reservation.domain.ReservationStatus;
import com.se.lsrs.reservation.dto.CancelReservationRequest;
import com.se.lsrs.reservation.dto.MyReservationResponse;
import com.se.lsrs.reservation.service.ReservationService;

import jakarta.validation.Valid;

/**
 * 학생 예약 API (설계서 4장). 이 클래스에는 US-08 엔드포인트만 있다.
 * POST /api/reservations(US-06), GET /api/reservations/{id}(US-06·09)는 각 담당이 추가한다.
 */
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

	private final ReservationService reservationService;

	public ReservationController(ReservationService reservationService) {
		this.reservationService = reservationService;
	}

	/** 내 예약 이력. status 생략 시 전체 (US-08 AC1·2) */
	@GetMapping("/me")
	public List<MyReservationResponse> getMine(@RequestParam(required = false) ReservationStatus status) {
		return reservationService.getMyReservations(status);
	}

	/** 직접 취소 (US-08 AC3~5) */
	@PutMapping("/{reservationId}/cancel")
	public MyReservationResponse cancel(@PathVariable Long reservationId,
			@Valid @RequestBody CancelReservationRequest request) {
		return reservationService.cancelByStudent(reservationId, request.reason());
	}

}
