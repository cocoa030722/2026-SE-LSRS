import { useCallback, useMemo, useState } from 'react';
import { AuthContext } from './authContext.js';
import { decodeUser, tokenStorage } from './tokenStorage.js';
import { authApi } from '../api/authApi.js';
import { env } from '../config/env.js';

function initialUser() {
  const token = tokenStorage.getAccessToken();
  if (token) {
    const restored = decodeUser(token);
    if (restored) return restored;
    tokenStorage.clear(); // 만료·손상된 토큰
  }
  // [개발 전용] .env.local 에 VITE_DEV_MOCK_ROLE=STUDENT|ADMIN 이면 토큰 없이 메뉴만 확인한다
  if (env.devMockRole) {
    return { loginId: `dev-${env.devMockRole.toLowerCase()}`, role: env.devMockRole };
  }
  return null;
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(initialUser);

  /** US-02 AC1: 로그인 → Access 토큰 저장 → 사용자(역할)를 토큰에서 복원. 실패하면 서버 오류를 그대로 던진다. */
  const login = useCallback(async ({ loginId, password }) => {
    const response = await authApi.login({ loginId, password });
    const accessToken = response.data?.accessToken;
    const loggedIn = accessToken ? decodeUser(accessToken) : null;
    if (!loggedIn) throw new Error('로그인 응답을 해석할 수 없습니다.');
    tokenStorage.save({ accessToken });
    setUser(loggedIn);
    return loggedIn;
  }, []);

  /** FR-U-14: 저장된 토큰을 지운다 (서버 측 로그아웃 API 는 없다: Sprint 2) */
  const logout = useCallback(async () => {
    tokenStorage.clear();
    setUser(null);
  }, []);

  const value = useMemo(
    () => ({ user, isAuthenticated: user !== null, login, logout }),
    [user, login, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
