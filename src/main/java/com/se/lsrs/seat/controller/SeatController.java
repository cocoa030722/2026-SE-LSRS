package com.se.lsrs.seat.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.se.lsrs.seat.dto.SeatStatusResponse;
import com.se.lsrs.seat.service.SeatQueryService;

/** 좌석 현황 조회 (설계서 4장 GET /api/seats, US-05) */
@RestController
@RequestMapping("/api/seats")
public class SeatController {

	private final SeatQueryService seatQueryService;

	public SeatController(SeatQueryService seatQueryService) {
		this.seatQueryService = seatQueryService;
	}

	/**
	 * 예: GET /api/seats?date=2026-12-01&amp;start=13:00&amp;end=15:00
	 *
	 * @param start 시작 시각 "HH:mm"
	 * @param end 종료 시각 "HH:mm" ("24:00" 허용)
	 */
	@GetMapping
	public List<SeatStatusResponse> getSeats(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, @RequestParam String start,
			@RequestParam String end) {
		return seatQueryService.getSeats(date, start, end);
	}

}
