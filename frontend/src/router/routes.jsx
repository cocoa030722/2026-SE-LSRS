import { paths } from './paths.js';
import { Role } from '../constants/roles.js';
import RequireAuth from '../auth/RequireAuth.jsx';
import RootRedirect from './RootRedirect.jsx';

import AuthLayout from '../layouts/AuthLayout.jsx';
import MainLayout from '../layouts/MainLayout.jsx';
import KioskLayout from '../layouts/KioskLayout.jsx';

import LoginPage from '../pages/auth/LoginPage.jsx';
import SignupPage from '../pages/auth/SignupPage.jsx';
import VerifyEmailPage from '../pages/auth/VerifyEmailPage.jsx';
import PasswordResetRequestPage from '../pages/auth/PasswordResetRequestPage.jsx';
import PasswordResetConfirmPage from '../pages/auth/PasswordResetConfirmPage.jsx';

import SeatMapPage from '../pages/student/SeatMapPage.jsx';
import MyReservationsPage from '../pages/student/MyReservationsPage.jsx';
import ReservationDetailPage from '../pages/student/ReservationDetailPage.jsx';

import AccountPage from '../pages/account/AccountPage.jsx';

import DashboardPage from '../pages/admin/DashboardPage.jsx';
import StudentManagementPage from '../pages/admin/StudentManagementPage.jsx';
import PolicyManagementPage from '../pages/admin/PolicyManagementPage.jsx';
import StatisticsPage from '../pages/admin/StatisticsPage.jsx';
import UserRoleManagementPage from '../pages/admin/UserRoleManagementPage.jsx';

import CheckInKioskPage from '../pages/kiosk/CheckInKioskPage.jsx';

import ForbiddenPage from '../pages/error/ForbiddenPage.jsx';
import NotFoundPage from '../pages/error/NotFoundPage.jsx';

/**
 * 라우트 트리
 *
 *  /                       → 역할별 첫 화면으로 이동
 *  [AuthLayout]            공개 화면 (E1)
 *  [RequireAuth]           로그인 필요
 *    [MainLayout]
 *      [RequireAuth STUDENT]  /seats, /reservations, /reservations/:id
 *      /account               학생·관리자 공통
 *      [RequireAuth ADMIN]    /admin/**
 *  [KioskLayout]           /kiosk  체크인 단말기 (단말기 키 인증, 사용자 로그인 없음)
 */
export const routes = [
  { path: paths.root, element: <RootRedirect /> },

  {
    element: <AuthLayout />,
    children: [
      { path: paths.login, element: <LoginPage /> },
      { path: paths.signup, element: <SignupPage /> },
      { path: paths.verifyEmail, element: <VerifyEmailPage /> },
      { path: paths.passwordReset, element: <PasswordResetRequestPage /> },
      { path: paths.passwordResetConfirm, element: <PasswordResetConfirmPage /> },
    ],
  },

  {
    element: <RequireAuth />,
    children: [
      {
        element: <MainLayout />,
        children: [
          {
            element: <RequireAuth roles={[Role.STUDENT]} />,
            children: [
              { path: paths.seats, element: <SeatMapPage /> },
              { path: paths.myReservations, element: <MyReservationsPage /> },
              { path: paths.reservationDetail(), element: <ReservationDetailPage /> },
            ],
          },
          { path: paths.account, element: <AccountPage /> },
          {
            element: <RequireAuth roles={[Role.ADMIN]} />,
            children: [
              { path: paths.adminDashboard, element: <DashboardPage /> },
              { path: paths.adminStudents, element: <StudentManagementPage /> },
              { path: paths.adminPolicies, element: <PolicyManagementPage /> },
              { path: paths.adminStatistics, element: <StatisticsPage /> },
              { path: paths.adminUsers, element: <UserRoleManagementPage /> },
            ],
          },
        ],
      },
    ],
  },

  {
    element: <KioskLayout />,
    children: [{ path: paths.kiosk, element: <CheckInKioskPage /> }],
  },

  { path: paths.forbidden, element: <ForbiddenPage /> },
  { path: '*', element: <NotFoundPage /> },
];
