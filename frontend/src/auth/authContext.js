import { createContext } from 'react';

/**
 * 인증 상태 컨텍스트.
 * value: { user: { loginId, role } | null, isAuthenticated, login(credentials), logout() }
 */
export const AuthContext = createContext(null);
