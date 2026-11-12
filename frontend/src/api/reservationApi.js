import { apiClient } from './client.js';

/** 학생 예약 API (설계서 4장, US-06·08·09) */
export const reservationApi = {
  /** US-06: 예약 생성 */
  create: (body) => apiClient.post('/reservations', body),
  /** US-08: 내 예약 이력. params: { status } */
  getMine: (params) => apiClient.get('/reservations/me', { params }),
  /** US-08·09: 예약 상세 + QR (본인만) */
  getById: (reservationId) => apiClient.get(`/reservations/${reservationId}`),
  /** US-08: 직접 취소. body: { reason } */
  cancel: (reservationId, body) => apiClient.put(`/reservations/${reservationId}/cancel`, body),
};
