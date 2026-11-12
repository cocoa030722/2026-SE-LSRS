package com.se.lsrs.reservation.domain;

import java.time.LocalDateTime;

import com.se.lsrs.seat.domain.Seat;
import com.se.lsrs.user.domain.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * 예약 (설계서 3장 Reservation).
 * 생성(US-06)은 해당 담당이 구현한다. 상태 전이는 WHERE status='RESERVED' 조건부 UPDATE로 수행한다.
 */
@Entity
@Table(indexes = @Index(name = "idx_reservation_user_start", columnList = "user_id, start_at"))
public class Reservation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long reservationId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "seat_id", nullable = false)
	private Seat seat;

	@Column(nullable = false)
	private LocalDateTime startAt;

	@Column(nullable = false)
	private LocalDateTime endAt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ReservationStatus status;

	private LocalDateTime checkedInAt;

	@Enumerated(EnumType.STRING)
	private CancelReason cancelReason;

	@Enumerated(EnumType.STRING)
	private CanceledBy canceledBy;

	@Column(nullable = false)
	private boolean penaltyApplied;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	protected Reservation() {
	}

	public Long getReservationId() {
		return reservationId;
	}

	public User getUser() {
		return user;
	}

	public Seat getSeat() {
		return seat;
	}

	public LocalDateTime getStartAt() {
		return startAt;
	}

	public LocalDateTime getEndAt() {
		return endAt;
	}

	public ReservationStatus getStatus() {
		return status;
	}

	public CancelReason getCancelReason() {
		return cancelReason;
	}

	public CanceledBy getCanceledBy() {
		return canceledBy;
	}

	public boolean isPenaltyApplied() {
		return penaltyApplied;
	}

}
