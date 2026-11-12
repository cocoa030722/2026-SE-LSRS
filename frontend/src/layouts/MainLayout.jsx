import { NavLink, Outlet } from 'react-router';
import { useAuth } from '../auth/useAuth.js';
import { Role } from '../constants/roles.js';
import { paths } from '../router/paths.js';

const NAV_ITEMS = {
  [Role.STUDENT]: [
    { to: paths.seats, label: '좌석 예약' },
    { to: paths.myReservations, label: '내 예약' },
    { to: paths.account, label: '내 계정' },
  ],
  [Role.ADMIN]: [
    { to: paths.adminDashboard, label: '대시보드', end: true },
    { to: paths.adminStudents, label: '학생 관리' },
    { to: paths.adminPolicies, label: '정책 관리' },
    { to: paths.adminStatistics, label: '사용 통계' },
    { to: paths.adminUsers, label: '권한 관리' },
    { to: paths.account, label: '내 계정' },
  ],
};

/** 로그인 사용자 공통 레이아웃. 역할에 따라 메뉴를 바꾼다. */
export default function MainLayout() {
  const { user } = useAuth();
  const navItems = NAV_ITEMS[user?.role] ?? [];

  return (
    <div className="main-layout">
      <header className="app-header">
        <span className="brand">LSRS</span>
        <nav className="app-nav">
          {navItems.map((item) => (
            <NavLink key={item.to} to={item.to} end={item.end}>
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className="app-user">
          <span>{user?.loginId}</span>
          {/* TODO(US-02, FR-U-14): onClick 에서 useAuth().logout 호출 후 로그인 화면으로 이동 */}
          <button type="button">
            로그아웃
          </button>
        </div>
      </header>
      <main className="app-content">
        <Outlet />
      </main>
    </div>
  );
}
