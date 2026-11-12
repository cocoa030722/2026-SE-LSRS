package com.se.lsrs.reservation.domain;

/**
 * 취소 사유 (Reservation.cancelReason).
 * 학생 직접 취소 사유: FR-X-02 / US-08 AC3.
 * 관리자 강제 취소 사유(FR-X-21)는 US-14 담당이 추가한다.
 */
public enum CancelReason {
	/** 일정 변경 */
	SCHEDULE_CHANGE,
	/** 좌석 위치 변경 */
	SEAT_CHANGE,
	/** 시간대 변경 */
	TIME_CHANGE,
	/** 기타 */
	OTHER
}
