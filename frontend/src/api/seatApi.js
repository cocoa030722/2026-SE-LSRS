import { apiClient } from './client.js';

/** 좌석 API (설계서 4장, US-05) */
export const seatApi = {
  /**
   * US-05: 좌석 그리드·4가지 상태. params: { date: 'YYYY-MM-DD', start: 'HH:00', end: 'HH:00' } ('24:00' 허용)
   * 응답 항목: { seatId, seatNo, zone, gridRow, gridCol, status } (status: constants/seatStatus SeatStatus)
   */
  getSeats: (params) => apiClient.get('/seats', { params }),
};
