import { Outlet } from 'react-router';

/**
 * 라우트 접근 제어 자리 (US-02 AC3·4, FR-U-12).
 * 화면 단 차단은 사용 편의용이며, 실제 권한 검사는 서버(SecurityConfig)가 담당한다.
 *
 * TODO(US-02):
 *  - 미인증 → paths.login 으로 이동 (돌아올 위치를 state.from 으로 전달)
 *  - roles 에 user.role 이 없으면 → paths.forbidden 으로 이동
 *
 * @param {{ roles?: string[] }} props 허용 역할 목록. 생략하면 로그인만 요구한다.
 */
export default function RequireAuth() {
  return <Outlet />;
}
