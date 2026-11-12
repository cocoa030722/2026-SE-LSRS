/**
 * 좌석 표시 상태.
 * - 학생 좌석 그리드 4종: FR-R-02 / US-05 AC2
 * - 관리자 축소 그리드 3종: FR-A-02 / US-12 AC2
 *
 * TODO(US-05, US-12): 코드 값은 가칭이다. 백엔드 응답 스키마가 확정되면 값을 맞춘다.
 */
export const SeatStatus = Object.freeze({
  AVAILABLE: 'AVAILABLE',
  IN_USE: 'IN_USE',
  MINE: 'MINE',
  RESERVED: 'RESERVED',
});

export const SEAT_STATUS_LABEL = Object.freeze({
  [SeatStatus.AVAILABLE]: '예약 가능',
  [SeatStatus.IN_USE]: '사용 중',
  [SeatStatus.MINE]: '내 예약',
  [SeatStatus.RESERVED]: '예약됨',
});

export const DashboardSeatStatus = Object.freeze({
  EMPTY: 'EMPTY',
  IN_USE: 'IN_USE',
  CHECKIN_PENDING: 'CHECKIN_PENDING',
});

export const DASHBOARD_SEAT_STATUS_LABEL = Object.freeze({
  [DashboardSeatStatus.EMPTY]: '빈 좌석',
  [DashboardSeatStatus.IN_USE]: '사용 중',
  [DashboardSeatStatus.CHECKIN_PENDING]: '체크인 대기',
});
