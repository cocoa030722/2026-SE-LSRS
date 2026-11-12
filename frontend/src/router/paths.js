/** 화면 경로 상수. 링크·리다이렉트는 문자열 대신 이 값을 쓴다. */
export const paths = {
  root: '/',

  // 공개 (E1)
  login: '/login',
  signup: '/signup',
  verifyEmail: '/verify-email',
  passwordReset: '/password-reset',
  passwordResetConfirm: '/password-reset/confirm',

  // 학생
  seats: '/seats',
  myReservations: '/reservations',
  reservationDetail: (reservationId = ':reservationId') => `/reservations/${reservationId}`,

  // 공통 (로그인 사용자)
  account: '/account',

  // 관리자 (E5, US-07)
  adminDashboard: '/admin',
  adminStudents: '/admin/students',
  adminPolicies: '/admin/policies',
  adminStatistics: '/admin/statistics',
  adminUsers: '/admin/users',

  // 체크인 단말기 (US-09)
  kiosk: '/kiosk',

  forbidden: '/403',
};
