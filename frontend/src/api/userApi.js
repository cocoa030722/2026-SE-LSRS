import { apiClient } from './client.js';

/** 내 계정 API (설계서 4장, US-03·04) */
export const userApi = {
  /** US-03: 비밀번호 변경 (전 세션 무효화) */
  changePassword: (body) => apiClient.put('/users/me/password', body),
  /** US-04: 회원 탈퇴 */
  withdraw: () => apiClient.delete('/users/me'),
};
