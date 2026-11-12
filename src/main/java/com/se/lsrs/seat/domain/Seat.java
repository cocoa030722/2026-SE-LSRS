package com.se.lsrs.seat.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * 좌석 (설계서 3장 Seat). 테이블 매핑만 둔다. 좌석 조회(US-05)는 해당 담당이 구현한다.
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

}
