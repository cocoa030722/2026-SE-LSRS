import { Outlet } from 'react-router';

/** 로그인·가입 등 공개 화면 레이아웃 */
export default function AuthLayout() {
  return (
    <div className="auth-layout">
      <div className="auth-card">
        <h1 className="brand">도서관 좌석 예약 시스템</h1>
        <Outlet />
      </div>
    </div>
  );
}
