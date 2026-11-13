package com.se.lsrs.seat.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.se.lsrs.seat.domain.Seat;

import jakarta.persistence.LockModeType;

public interface SeatRepository extends JpaRepository<Seat, Long> {

	/** 활성 좌석을 그리드 순서로 (US-05 AC1) */
	List<Seat> findAllByActiveTrueOrderByGridRowAscGridColAsc();

	/**
	 * 좌석 행을 비관적 잠금(SELECT … FOR UPDATE)으로 조회한다. 가변 길이 시간 구간은 UNIQUE 로 막을 수 없어
	 * 좌석을 잠근 뒤 겹침을 조회해 이중 배정을 막는다 (설계서 Core Flow 1, US-06 AC4).
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select s from Seat s where s.seatId = :seatId")
	Optional<Seat> findByIdForUpdate(Long seatId);

}
