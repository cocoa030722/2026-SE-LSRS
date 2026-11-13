package com.se.lsrs.seat.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * 좌석 (설계서 3장 Seat). 좌석 표시 상태는 저장하지 않고 진행 중 예약으로 계산한다 (SeatQueryService, US-05).
 * 비활성(active=false) 좌석은 조회·예약 대상이 아니다.
 */
@Entity
public class Seat {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long seatId;

	@Column(nullable = false, unique = true)
	private String seatNo;

	private String zone;

	@Column(nullable = false)
	private int gridRow;

	@Column(nullable = false)
	private int gridCol;

	@Column(nullable = false)
	private boolean active;

	protected Seat() {
	}

	public Long getSeatId() {
		return seatId;
	}

	public String getSeatNo() {
		return seatNo;
	}

	public String getZone() {
		return zone;
	}

	public int getGridRow() {
		return gridRow;
	}

	public int getGridCol() {
		return gridCol;
	}

	public boolean isActive() {
		return active;
	}

}
