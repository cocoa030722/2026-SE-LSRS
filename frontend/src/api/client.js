import axios from 'axios';
import { env } from '../config/env.js';
import { tokenStorage } from '../auth/tokenStorage.js';
import { paths } from '../router/paths.js';

/**
 * 사용자(학생·관리자) API 공용 Axios 인스턴스.
 * 설계서 2장: "Axios REST 호출 + JWT 헤더. 업무 규칙은 두지 않음"
 */
export const apiClient = axios.create({
  baseURL: env.apiBaseUrl,
  headers: { 'Content-Type': 'application/json' },
});

/** 로그인 전에 쓰는 API: 여기서의 401 은 "로그인 실패"이므로 화면 이동 없이 호출한 쪽이 처리한다 */
const isPublicAuthCall = (config) => String(config?.url ?? '').startsWith('/auth/');

// US-02 AC1: 이후 API 요청은 토큰으로 인증된다
apiClient.interceptors.request.use((config) => {
  // 로그인·가입·인증 요청에는 붙이지 않는다: 서버에서 무효가 된 토큰이 남아 있어도 로그인할 수 있어야 한다
  const token = tokenStorage.getAccessToken();
  if (token && !isPublicAuthCall(config)) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status;
    // US-02 AC3: 토큰 없음·만료·무효 → 토큰 삭제 후 로그인 화면
    if (status === 401 && !isPublicAuthCall(error.config)) {
      tokenStorage.clear();
      if (window.location.pathname !== paths.login) window.location.assign(paths.login);
    }
    // US-02 AC4: 역할 권한 거부(ACCESS_DENIED)만 권한 없음 화면으로. 이용 제한(USER_RESTRICTED) 등 업무상 403 은 화면에서 안내한다.
    if (status === 403 && error.response?.data?.code === 'ACCESS_DENIED') {
      window.location.assign(paths.forbidden);
    }
    return Promise.reject(error);
  },
);
