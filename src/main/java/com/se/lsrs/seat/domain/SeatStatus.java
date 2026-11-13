package com.se.lsrs.seat.domain;

/**
 * 학생 좌석 그리드의 표시 상태 (FR-R-02, US-05 AC2). 저장하지 않고 조회 시간대의 진행 중 예약으로 계산한다.
 * 한 좌석에 겹치는 예약이 여럿이면 MINE &gt; IN_USE &gt; RESERVED 순으로 표시한다.
 */
public enum SeatStatus {
	/** 예약 가능: 겹치는 진행 중 예약 없음 */
	AVAILABLE,
	/** 사용 중: 다른 학생이 체크인(CHECKED_IN)한 예약과 겹침 */
	IN_USE,
	/** 내 예약: 본인의 진행 중 예약과 겹침 */
	MINE,
	/** 예약됨: 다른 학생의 RESERVED 예약과 겹침 */
	RESERVED
}
