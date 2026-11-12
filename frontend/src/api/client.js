import axios from 'axios';
import { env } from '../config/env.js';

/**
 * 사용자(학생·관리자) API 공용 Axios 인스턴스.
 * 설계서 2장: "Axios REST 호출 + JWT 헤더. 업무 규칙은 두지 않음"
 */
export const apiClient = axios.create({
  baseURL: env.apiBaseUrl,
  headers: { 'Content-Type': 'application/json' },
});

apiClient.interceptors.request.use((config) => {
  // TODO(US-02 AC1): tokenStorage 의 Access 토큰을 Authorization: Bearer 헤더로 붙인다.
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    // TODO(US-02 AC3): 401 → Refresh 토큰 재발급 시도(POST /auth/token/refresh),
    //   실패 시 토큰 삭제 후 로그인 화면으로 이동.
    // TODO(US-02 AC4): 403 → 권한 없음 안내 화면으로 이동.
    return Promise.reject(error);
  },
);
