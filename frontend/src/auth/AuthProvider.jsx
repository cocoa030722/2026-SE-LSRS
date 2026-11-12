import { useMemo, useState } from 'react';
import { AuthContext } from './authContext.js';
import { env } from '../config/env.js';

function initialUser() {
  // [개발 전용] 로그인 구현 전 화면 확인용. .env.local 에 VITE_DEV_MOCK_ROLE=STUDENT|ADMIN
  if (env.devMockRole) {
    return { loginId: `dev-${env.devMockRole.toLowerCase()}`, role: env.devMockRole };
  }
  // TODO(US-02): 저장된 토큰이 있으면 사용자 정보(역할)를 복원한다.
  return null;
}

export function AuthProvider({ children }) {
  const [user] = useState(initialUser);

  const value = useMemo(
    () => ({
      user,
      isAuthenticated: user !== null,
      // TODO(US-02 AC1·2): authApi.login 호출 → tokenStorage.save → user 설정
      login: async () => {
        throw new Error('TODO(US-02): 로그인 미구현');
      },
      // TODO(US-02, FR-U-14): authApi.logout 호출 → tokenStorage.clear → user 초기화
      logout: async () => {
        throw new Error('TODO(US-02): 로그아웃 미구현');
      },
    }),
    [user],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
