/**
 * 예약 상태 (설계서 3장 Reservation.status, FR-R-13·FR-C-03·FR-X-10).
 * 라벨은 FR-R-20 / US-08 AC1 표기를 따른다.
 */
export const ReservationStatus = Object.freeze({
  RESERVED: 'RESERVED',
  CHECKED_IN: 'CHECKED_IN',
  CANCELLED: 'CANCELLED',
  AUTO_CANCELLED: 'AUTO_CANCELLED',
});

export const RESERVATION_STATUS_LABEL = Object.freeze({
  [ReservationStatus.RESERVED]: '예약중',
  [ReservationStatus.CHECKED_IN]: '체크인 완료',
  [ReservationStatus.CANCELLED]: '취소됨',
  [ReservationStatus.AUTO_CANCELLED]: '노쇼',
});
