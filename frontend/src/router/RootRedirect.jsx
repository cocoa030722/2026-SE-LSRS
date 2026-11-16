import { Navigate } from 'react-router';
import { useAuth } from '../auth/useAuth.js';
import { Role } from '../constants/roles.js';
import { paths } from './paths.js';

/** 역할별 첫 화면 */
export function homePathFor(role) {
  return role === Role.ADMIN ? paths.adminPolicies : paths.seats;
}

/** "/" 진입: 미인증 → 로그인, ADMIN → 정책 관리, STUDENT → 좌석 예약 */
export default function RootRedirect() {
  const { user } = useAuth();
  return <Navigate to={user ? homePathFor(user.role) : paths.login} replace />;
}
