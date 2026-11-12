import { apiClient } from './client.js';

/** 좌석 API (설계서 4장, US-05) */
export const seatApi = {
  /** US-05: 좌석 그리드·4가지 상태. params: { date, start, end } */
  getSeats: (params) => apiClient.get('/seats', { params }),
};
