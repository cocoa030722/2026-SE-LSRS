import { Navigate, Outlet, useLocation } from 'react-router';
import { useAuth } from './useAuth.js';
import { paths } from '../router/paths.js';

/**
 * 라우트 접근 제어 (US-02 AC3·4, FR-U-12).
 * 화면 단 차단은 사용 편의용이며, 실제 권한 검사는 서버(SecurityConfig)가 담당한다.
 *  - 미인증 → 로그인 화면 (돌아올 위치를 state.from 으로 전달)
 *  - roles 에 user.role 이 없으면 → 권한 없음 화면
 *
 * @param {{ roles?: string[] }} props 허용 역할 목록. 생략하면 로그인만 요구한다.
 */
export default function RequireAuth({ roles }) {
  const { user } = useAuth();
  const location = useLocation();

  if (!user) {
    return <Navigate to={paths.login} replace state={{ from: location.pathname }} />;
  }
  if (roles && !roles.includes(user.role)) {
    return <Navigate to={paths.forbidden} replace />;
  }
  return <Outlet />;
}
