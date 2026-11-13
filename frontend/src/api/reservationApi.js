import { apiClient } from './client.js';

/** 학생 예약 API (설계서 4장, US-06·08·09) */
export const reservationApi = {
  /**
   * US-06: 예약 생성. body: { seatId, startAt, endAt } (정시 'YYYY-MM-DDTHH:00:00', utils/timeSlot toLocalDateTime)
   * 응답: { reservationId, seatId, seatNo, startAt, endAt, status, createdAt } (201)
   */
  create: (body) => apiClient.post('/reservations', body),
  /**
   * US-08: 내 예약 이력(최신순). params: { status } (생략 시 전체)
   * 응답 항목: { reservationId, seatNo, startAt, endAt, status, cancelReason, cancellable }
   */
  getMine: (params) => apiClient.get('/reservations/me', { params }),
  /** US-08·09: 예약 상세 + QR (본인만) */
  getById: (reservationId) => apiClient.get(`/reservations/${reservationId}`),
  /** US-08: 직접 취소. body: { reason } (constants/cancelReasons STUDENT_CANCEL_REASONS 의 code) */
  cancel: (reservationId, body) => apiClient.put(`/reservations/${reservationId}/cancel`, body),
};
