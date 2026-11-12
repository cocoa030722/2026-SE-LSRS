package com.se.lsrs.reservation.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.se.lsrs.reservation.domain.ReservationStatus;
import com.se.lsrs.reservation.dto.CancelReservationRequest;
import com.se.lsrs.reservation.dto.CreateReservationRequest;
import com.se.lsrs.reservation.dto.MyReservationResponse;
import com.se.lsrs.reservation.dto.ReservationResponse;
import com.se.lsrs.reservation.service.ReservationService;

import jakarta.validation.Valid;

/**
 * 학생 예약 API (설계서 4장). US-06·08 엔드포인트가 있다.
 * GET /api/reservations/{id}(예약 상세 + QR, US-09)는 해당 담당이 추가한다.
 */
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

	private final ReservationService reservationService;

	public ReservationController(ReservationService reservationService) {
		this.reservationService = reservationService;
	}

	/** 예약 생성 (US-06). 좌석·시간대 미선택은 400 (AC2) */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ReservationResponse create(@Valid @RequestBody CreateReservationRequest request) {
		return reservationService.reserve(request.seatId(), request.startAt(), request.endAt());
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
