import { apiClient } from './client.js';

/** 인증 API (설계서 4장, US-01·02·03). 요청/응답 DTO는 백엔드 구현 시 확정한다. */
export const authApi = {
  /** US-01: 가입 신청 → PENDING, 인증 메일 발송 */
  signup: (body) => apiClient.post('/auth/signup', body),
  /** US-01: 인증 코드 확인 → ACTIVE */
  verifyEmail: (body) => apiClient.post('/auth/verify-email', body),
  /** US-02: 로그인 (Access 1h / Refresh 7d) */
  login: (body) => apiClient.post('/auth/login', body),
  /** US-02: Access 토큰 재발급 */
  refreshToken: (body) => apiClient.post('/auth/token/refresh', body),
  /** US-02: 로그아웃 (Refresh 폐기) */
  logout: (body) => apiClient.post('/auth/logout', body),
  /** US-03: 비밀번호 재설정 메일 요청 */
  requestPasswordReset: (body) => apiClient.post('/auth/password-reset', body),
  /** US-03: 새 비밀번호 설정 */
  resetPassword: (body) => apiClient.put('/auth/password-reset', body),
};
