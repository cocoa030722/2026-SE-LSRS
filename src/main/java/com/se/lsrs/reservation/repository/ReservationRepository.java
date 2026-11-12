package com.se.lsrs.reservation.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.se.lsrs.reservation.domain.CancelReason;
import com.se.lsrs.reservation.domain.Reservation;
import com.se.lsrs.reservation.domain.ReservationStatus;

/**
 * 진행 중 예약 = status ∈ {RESERVED, CHECKED_IN} AND endAt &gt; now (설계서 3장).
 * 시간 구간 [startAt, endAt) 겹침 = r.startAt &lt; endAt AND r.endAt &gt; startAt (끝과 시작이 맞닿으면 겹치지 않음).
 */
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

	/** [startAt, endAt) 와 겹치는 진행 중 예약 전체. 좌석 그리드 상태 계산에 쓴다 (US-05) */
	@Query("""
			select new com.se.lsrs.reservation.repository.SeatOccupancy(r.seat.seatId, r.user.userId, r.status)
			from Reservation r
			where r.status in (com.se.lsrs.reservation.domain.ReservationStatus.RESERVED,
					com.se.lsrs.reservation.domain.ReservationStatus.CHECKED_IN)
				and r.endAt > :now
				and r.startAt < :endAt and r.endAt > :startAt
			""")
	List<SeatOccupancy> findActiveOverlapping(LocalDateTime startAt, LocalDateTime endAt, LocalDateTime now);

	/** 좌석에 [startAt, endAt) 와 겹치는 진행 중 예약이 있는지 (US-06 AC4). 좌석 행 잠금 뒤 호출한다. */
	@Query("""
			select count(r) > 0 from Reservation r
			where r.seat.seatId = :seatId
				and r.status in (com.se.lsrs.reservation.domain.ReservationStatus.RESERVED,
					com.se.lsrs.reservation.domain.ReservationStatus.CHECKED_IN)
				and r.endAt > :now
				and r.startAt < :endAt and r.endAt > :startAt
			""")
	boolean existsActiveOverlapBySeat(Long seatId, LocalDateTime startAt, LocalDateTime endAt, LocalDateTime now);

	/** 학생에게 [startAt, endAt) 와 겹치는 진행 중 예약이 있는지 (US-06 AC2, FR-R-11). 학생 행 잠금 뒤 호출한다. */
	@Query("""
			select count(r) > 0 from Reservation r
			where r.user.userId = :userId
				and r.status in (com.se.lsrs.reservation.domain.ReservationStatus.RESERVED,
					com.se.lsrs.reservation.domain.ReservationStatus.CHECKED_IN)
				and r.endAt > :now
				and r.startAt < :endAt and r.endAt > :startAt
			""")
	boolean existsActiveOverlapByUser(Long userId, LocalDateTime startAt, LocalDateTime endAt, LocalDateTime now);

	/** 내 예약 이력, 최신순(시작 시각 내림차순). status 가 null 이면 전체 (US-08 AC1·2) */
	@Query("""
			select r from Reservation r join fetch r.seat
			where r.user.userId = :userId and (:status is null or r.status = :status)
			order by r.startAt desc, r.reservationId desc
			""")
	List<Reservation> findMine(Long userId, ReservationStatus status);

	/** 본인 예약만 조회한다. 다른 학생의 예약은 존재 여부도 드러내지 않는다 (US-02 AC5, NFR 4.5). */
	Optional<Reservation> findByReservationIdAndUserUserId(Long reservationId, Long userId);

	/**
	 * 학생 직접 취소 (US-08 AC3·4). RESERVED 이고 시작 시각이 startAtFrom 이후(= 체크인 가능 시간 이내)일 때만
	 * CANCELLED 로 바꾸는 조건부 UPDATE. 체크인·자동 취소와 경합해도 한쪽만 반영된다 (설계서 3장).
	 *
	 * @return 반영된 행 수 (0 이면 이미 상태가 바뀐 예약)
	 */
	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("""
			update Reservation r
			set r.status = com.se.lsrs.reservation.domain.ReservationStatus.CANCELLED,
				r.cancelReason = :reason,
				r.canceledBy = com.se.lsrs.reservation.domain.CanceledBy.STUDENT,
				r.penaltyApplied = false
			where r.reservationId = :reservationId
				and r.status = com.se.lsrs.reservation.domain.ReservationStatus.RESERVED
				and r.startAt >= :startAtFrom
			""")
	int cancelByStudent(Long reservationId, CancelReason reason, LocalDateTime startAtFrom);

}
