# LSRS Frontend

도서관 좌석 예약 시스템의 React 클라이언트. 현재는 **스켈레톤**이다. 라우트 트리·레이아웃·API 엔드포인트 모듈·화면/컴포넌트 골격만 있고, 권한 가드·토큰 처리·각 화면 기능은 모두 `TODO(US-xx)` 주석과 화면의 점선 박스(`Placeholder`)로 표시되어 있다.

## 실행

```bash
cd frontend
npm install
cp .env.example .env.local   # 필요 시 값 수정
npm run dev                  # http://localhost:5173, /api → http://localhost:8080 프록시
```

권한 가드가 아직 없어 모든 경로에 바로 들어갈 수 있다. 상단 메뉴는 역할에 따라 바뀌므로, 메뉴를 보려면 `.env.local`에 `VITE_DEV_MOCK_ROLE=STUDENT` 또는 `ADMIN`을 넣는다(개발 서버에서만 동작).

버전은 `docs/기술스택_최신버전.md`를 따른다 (Node 26.13.1, React 19.3.1, Vite 8.3.8, React Router 8.5.1, Axios 1.21.1, qrcode.react 4.2.0, html5-qrcode 2.3.8).

## 구조

```
src/
  api/          Axios 인스턴스(client: JWT, deviceClient: 단말기 키)와 엔드포인트별 모듈 (설계서 4장)
  auth/         AuthProvider·useAuth(인증 상태), RequireAuth(역할별 라우트 가드 자리), tokenStorage — 모두 미구현 스텁
  router/       paths(경로 상수), routes(라우트 트리), router(createBrowserRouter), RootRedirect
  layouts/      AuthLayout(공개), MainLayout(학생·관리자 메뉴), KioskLayout(체크인 단말기)
  pages/        화면 단위. 데이터 조회·상태 보관을 맡는다
  components/   화면 조각. props만 받아 그린다 (common / seat / reservation / qr / admin)
  constants/    역할·예약 상태·좌석 상태·취소 사유
  hooks/        useAutoRefresh (대시보드 30초 갱신 등, 미구현 스텁)
  utils/        apiError ({ code, message } 오류 응답 해석, 미구현 스텁)
```

원칙: 업무 규칙(정책 판정, 중복 검사, 권한 판단)은 서버에 두고 클라이언트는 표시와 입력만 맡는다 (설계서 2장). 화면 단 권한 가드는 편의 기능일 뿐이다.

## 화면 ↔ User Story

| 경로 | 화면 | US |
|---|---|---|
| `/login` | LoginPage | US-02 |
| `/signup`, `/verify-email` | SignupPage, VerifyEmailPage | US-01 |
| `/password-reset`, `/password-reset/confirm` | PasswordReset*Page | US-03 |
| `/seats` | SeatMapPage | US-05, 06, 07 |
| `/reservations` | MyReservationsPage | US-08 |
| `/reservations/:id` | ReservationDetailPage (예약 완료·QR) | US-06, 09 |
| `/account` | AccountPage (비밀번호 변경·탈퇴) | US-03, 04 |
| `/admin` | DashboardPage | US-12 |
| `/admin/students` | StudentManagementPage (제한 관리·강제 취소) | US-13, 14 |
| `/admin/policies` | PolicyManagementPage (시험 기간·정책 파라미터) | US-07, 11 |
| `/admin/statistics` | StatisticsPage | US-15 |
| `/admin/users` | UserRoleManagementPage | US-16 |
| `/kiosk` | CheckInKioskPage | US-09 |

## 백엔드와 맞춰야 할 가칭

- 좌석 상태 코드 (`constants/seatStatus.js`)
- 취소 사유 코드 (`constants/cancelReasons.js`)
- 단말기 키 헤더 이름 (`api/deviceClient.js`)
- 시험 기간 삭제 경로, 정책 파라미터 목록 조회 출처 (`api/adminApi.js`, `PolicyManagementPage`)
