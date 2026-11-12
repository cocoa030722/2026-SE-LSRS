import { deviceClient } from './deviceClient.js';

/** 체크인 API (설계서 4장, US-09) — 단말기 키 인증 */
export const checkInApi = {
  /** body: { qrToken } */
  checkIn: (body) => deviceClient.post('/check-ins', body),
};
