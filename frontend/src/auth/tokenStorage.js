const ACCESS_TOKEN_KEY = 'lsrs.accessToken';

/**
 * 토큰 저장소. 저장 위치를 이 모듈 뒤로 숨겨 다른 코드가 저장 방식에 의존하지 않게 한다.
 * Access 토큰만 localStorage 에 둔다 (Refresh 토큰은 백엔드가 발급하지 않는다: Sprint 2).
 */
export const tokenStorage = {
  /** @returns {string | null} */
  getAccessToken: () => localStorage.getItem(ACCESS_TOKEN_KEY),
  /** @returns {string | null} */
  getRefreshToken: () => null,
  /** @param {{ accessToken?: string }} tokens */
  save(tokens) {
    if (tokens?.accessToken) localStorage.setItem(ACCESS_TOKEN_KEY, tokens.accessToken);
  },
  clear() {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
  },
};

/**
 * JWT payload 에서 사용자 정보를 꺼낸다 (서명 검증은 서버가 한다). 형식이 틀리거나 만료되면 null.
 * @returns {{ loginId: string, role: string } | null}
 */
export function decodeUser(token) {
  try {
    const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    const payload = JSON.parse(decodeURIComponent(escape(atob(base64))));
    if (!payload.sub || !payload.role) return null;
    if (payload.exp && payload.exp * 1000 <= Date.now()) return null;
    return { loginId: payload.sub, role: payload.role };
  } catch {
    return null;
  }
}
