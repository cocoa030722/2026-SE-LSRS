package com.se.lsrs.seat.dto;

import com.se.lsrs.seat.domain.Seat;
import com.se.lsrs.seat.domain.SeatStatus;

/** 좌석 그리드 항목 (US-05 AC1·2). gridRow·gridCol 위치에 status 색으로 표시한다. */
public record SeatStatusResponse(Long seatId, String seatNo, String zone, int gridRow, int gridCol,
		SeatStatus status) {

	public static SeatStatusResponse of(Seat seat, SeatStatus status) {
		return new SeatStatusResponse(seat.getSeatId(), seat.getSeatNo(), seat.getZone(), seat.getGridRow(),
				seat.getGridCol(), status);
	}

}
