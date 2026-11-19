# 미완료 US와 Sprint 2 계획

- 작성일: 2026-11-16
- 현행 기준: 브랜치 `fix/build-and-tests` (`daa3c53`)
- 근거: `Product_Backlog_final.docx` Release Plan, `System_Architecture_Design_final.docx` 6장 Traceability, `docs/과제 설명 파일/명세서.md` 1.3절
- 차이 상세는 [`diff.md`](diff.md)를 본다.

---

## 1. Sprint 1 미완료 현황

### 1.1 미완료 US: 없음
Sprint 1 계획 US 7개(28 SP)는 모두 구현했고, 화면 시연과 자동 테스트로 AC를 확인했다.

| US | Priority / SP | 상태 |
|---|---|---|
| US-01 회원가입·이메일 인증 | Must / 5 | 완료. AC1의 메일 발송만 이월 (1.2의 C1) |
| US-02 로그인·로그아웃·권한 분리 | Must / 3 | 완료. AC1 Refresh 토큰, AC3 서버 로그아웃 이월 (C2) |
| US-04 회원 탈퇴 | Should / 3 | 완료 |
| US-05 좌석 현황 조회 | Must / 3 | 완료 |
| US-06 예약 생성·동시 예약 방지 | Must / 8 | 완료 |
| US-07 예약 정책(시험 기간) | Must / 3 | 완료 |
| US-08 내 예약 조회·직접 취소 | Must / 3 | 완료 |

### 1.2 Sprint 2로 이월하는 항목
US는 완료했지만 AC 일부나 설계 세부가 남은 것이다. Sprint 2 US와 같은 인프라를 쓰므로 해당 US와 함께 처리한다.

| # | 항목 | 관련 | 함께 처리할 US | 할 일 |
|---|---|---|---|---|
| C1 | 인증 메일 발송 | US-01 AC1 | US-03 (재설정 메일) | `spring-boot-starter-mail` + `MailSender`(테스트는 Mock), `compose.yaml`에 Mailpit 1.33.1 추가, 시연용 학교 SMTP 설정. 콘솔 출력 제거 |
| C2 | Refresh 토큰(7일)·재발급·서버 로그아웃 | US-02 AC1·AC3 | US-03, US-16 (토큰 무효화) | `RefreshToken` 엔티티(tokenHash 저장), `POST /api/auth/token/refresh`, `POST /api/auth/logout`. 프론트 `authApi.refreshToken`·`logout` 연결, 401 시 1회 재발급 후 재시도 |
| C3 | VerificationToken 설계서 구조로 변경 | US-01 | US-03 | `userId(FK)·type(EMAIL_VERIFY/PASSWORD_RESET)·tokenHash·expiresAt·usedAt`. 사용 후 삭제 대신 `usedAt` 기록 |
| C4 | 학생 전용 API 서버 권한 검사 | US-02 AC4 | — | `/api/seats`, `/api/reservations/**`, `DELETE /api/users/me`에 `hasRole("STUDENT")`. 관리자 토큰 403 테스트 추가 |
| C5 | 이메일 인증의 `Clock` 사용 | NFR 4.2 | US-03 | `AuthService`·`VerificationToken`의 `LocalDateTime.now()`를 주입된 `Clock`으로 |
| C6 | JWT 데모용 기본 키 제거 | 보안 | — | 데모 직후 `0aeb98f` 되돌리기. `JWT_SECRET` 환경 변수 필수로 복귀 |
| C7 | 버전 맞춤 | `기술스택_최신버전.md` | C2와 함께 | jjwt 0.11.5 → 0.13.0(API 변경 반영), Gradle wrapper 9.8.2 → 9.9.1, Java toolchain 21 → 27 여부 팀 결정 |

### 1.3 Seed로 검증한 AC의 실제 연동 확인
Backlog "Sprint 간 의존성 처리"에 따라, Sprint 1에서 Seed 데이터로 확인한 AC를 Sprint 2 기능으로 실제 상태를 만든 뒤 통합 테스트로 다시 확인한다.

| Sprint 1 AC | Sprint 1 검증 방법 | Sprint 2 실제 연동 |
|---|---|---|
| US-04 AC2 이용 제한 회원 탈퇴 거부 | Seed: `restricted_until` 설정 | US-11 노쇼 3회 → 제한 → 탈퇴 거부 |
| US-06 AC3 이용 제한 학생 예약 거부 | Seed: 학생C | US-11 제한, US-13 관리자 제한 설정·해제 |
| US-08 AC1·2 체크인 완료·노쇼 표시와 필터 | Seed: `CHECKED_IN`·`AUTO_CANCELLED` 예약 | US-09 체크인, US-10 자동 취소로 만든 예약 |
| US-08 AC4 직접 취소 시 노쇼 카운터 불변 | Seed: 노쇼 1회 학생 | US-10 노쇼와 직접 취소를 섞은 시나리오 |
| US-05 AC2 '사용 중' 표시 | Seed: `CHECKED_IN` 예약 | US-09 체크인 직후 좌석 조회 |

---

## 2. Sprint 2 계획

### 2.1 목표
**Enhancement Sprint:** QR 체크인, 노쇼 자동 처리, 관리자 운영 기능으로 운영 가능한 MVP를 완성한다 (Backlog Release Plan, 명세서 1.3 "Sprint 2: Quality").

| Sprint 2 US | Epic | Priority | SP |
|---|---|---|---|
| US-03 비밀번호 변경·재설정 | E1 | Should | 3 |
| US-09 QR 발급 및 체크인 | E4 | Must | 5 |
| US-10 미체크인 예약 자동 취소(노쇼 처리) | E4 | Must | 5 |
| US-11 노쇼 누적에 따른 이용 제한 | E4 | Must | 3 |
| US-12 실시간 이용 현황 대시보드 | E5 | Must | 5 |
| US-13 학생 검색 및 이용 제한 관리 | E5 | Must | 3 |
| US-14 관리자 강제 취소 | E5 | Must | 2 |
| US-15 좌석 사용 통계 조회 | E5 | Should | 2 |
| US-16 관리자 권한 부여·회수 | E5 | Could | 2 |
| **합계** | | Must 23 / 전체 | **30** |

이월 항목(1.2의 C1~C7)은 별도 SP 없이 묶인 US에 포함한다. 일정이 모자라면 **Could(US-16) → Should(US-15, US-03)** 순으로 뺀다. C1·C2는 US-03을 빼더라도 남긴다(US-01·02 AC 충족이 걸려 있음).

### 2.2 US별 구현 요소
설계서 6장 Traceability 기준이다. "현행 준비 상태"는 지금 코드에 이미 있는 것이다.

| US | 백엔드 (API · Service · Domain) | 프론트엔드 (현재 Placeholder) | 현행 준비 상태 | 핵심 AC·주의 |
|---|---|---|---|---|
| **US-09** | `GET /api/reservations/{id}`(상세+QR, 본인만), `POST /api/check-ins` · `CheckInService`, `QrTokenProvider`(HMAC-SHA256), `DeviceKeyFilter` | `ReservationDetailPage`, `ReservationQrCode`, `CheckInKioskPage`, `QrScanner`, `deviceClient` | `Reservation.checkedInAt`, `CHECKIN_WINDOW_MIN=15`, qrcode.react·html5-qrcode 설치됨 | 15분 경과는 **체크인 시점에 재확인**(스케줄러와 무관, AC3). `WHERE status='RESERVED'` 조건부 UPDATE로 스케줄러와 경합 시 한쪽만 반영. 시작 전 스캔은 TOO_EARLY, 초과는 CHECKIN_EXPIRED(409) |
| **US-10** | `NoShowScheduler`(@Scheduled 10분) → `NoShowService.processNoShows()` | — | `ReservationStatus.AUTO_CANCELLED`, `CanceledBy.SYSTEM`, 인덱스 `(status, start_at)`, `Clock` Bean | 스케줄러는 서비스 호출만. 조건부 UPDATE 반영 1건일 때만 노쇼 반영(중복 증가 방지, AC5). 예약별 트랜잭션. **고정 Clock으로 14분 유지 / 15분 초과 취소 테스트**(AC4) |
| **US-11** | `User.addNoShow(now, policy)`, `User.release()`, 자동 해제 · 정책 키 `NOSHOW_THRESHOLD(3)`, `RESTRICTION_DAYS(3)` | 예약 거부 안내는 이미 있음 | `User.noShowCount·restrictedUntil·isRestricted()`, 예약 시 제한 검사(US-06 AC3) | 노쇼 반영은 `addNoShow()`로만, 해제는 `release()`로만(카운터 0). 정책 값 변경 시연(AC4). 잠금 순서 **예약 행 → User** (설계서 Core Flow 1·3) |
| **US-14** | `PUT /api/admin/reservations/{id}/cancel {reason, applyPenalty}` · `ReservationService.cancelByAdmin()` | `ForceCancelDialog` | `CanceledBy.ADMIN`, `penaltyApplied`, 학생 취소의 조건부 UPDATE 패턴 | `CancelReason`에 관리자 사유 4개 추가. 사유 필수(AC2). '부여'일 때만 `addNoShow()` (US-11 이후) |
| **US-13** | `GET /api/admin/students?studentNo=&seatNo=`, `PUT·DELETE .../restriction`, `DELETE .../no-show-count` · `AdminStudentService` | `StudentManagementPage`, `StudentSearchForm`, `StudentTable`, `RestrictionDialog` | `restrictedUntil` | 해제는 `release()` 공용. 과거 해제일·없는 학생·제한 아닌 학생 해제 거부(AC3) |
| **US-12** | `GET /api/admin/dashboard` · `DashboardService` | `DashboardPage`, `DashboardSummary`, `MiniSeatGrid` (30초 자동 갱신 훅 `useAutoRefresh` 있음) | 좌석 상태 계산(`SeatQueryService`), 인덱스 | 체크인 대기 = 시작 ≤ now ≤ 시작+15분인 RESERVED. 현재 가동률 = 이용 인원 ÷ 활성 좌석. 관리자 첫 화면을 정책 관리 → 대시보드로 변경 |
| **US-15** | `GET /api/admin/statistics?from=&to=` · `StatisticsService` | `StatisticsPage`, `StatisticsView` | `OPEN_HOUR·CLOSE_HOUR` | 가동률 = CHECKED_IN 점유 시간 합 ÷ (활성 좌석 × 운영 시간). 데이터 없으면 안내(AC4) |
| **US-03** | `PUT /api/users/me/password`, `POST·PUT /api/auth/password-reset` · `AuthService` | `AccountPage` 비밀번호 변경, `PasswordResetRequestPage`, `PasswordResetConfirmPage` | 비밀번호 규칙 `validatePassword`, `tokenVersion` | 변경 후 `tokenVersion` 증가 + Refresh 전부 폐기(AC1). 가입 여부 구분 불가한 동일 응답(AC4). C1·C2·C3·C5 함께 |
| **US-16** | `PUT /api/admin/users/{id}/role {role}` · `AdminUserService` | `UserRoleManagementPage`, `UserRoleForm` | 필터가 매 요청 DB의 role·tokenVersion 대조 | 변경 시 `tokenVersion` 증가(다음 로그인부터 반영). 마지막 관리자 회수 거부(AC3) |

### 2.3 진행 순서
의존성(체크인 상태 → 노쇼 → 제한 → 관리자 운영)과 위험도(스케줄러 시간 처리, 동시성)를 기준으로 한다.

| 단계 | 내용 | 이유 |
|---|---|---|
| **0. 정리** | C6(JWT 기본 키 제거), C4(학생 권한), C7(버전), PR 병합 후 `main` 기준으로 시작 | 데모용 임시 조치를 먼저 걷어 낸다 |
| **1. 공통 기반** | `User.addNoShow()·release()`, 정책 키 2개, 관리자 `CancelReason`, `MailSender`+Mailpit(C1), `RefreshToken`(C2) | US-10·11·13·14와 US-03이 공유한다. 먼저 합의하고 만들어 병합 충돌을 줄인다 |
| **2. E4 체크인·노쇼** | US-09 → US-10 → US-11 | 체크인 상태가 있어야 노쇼 판정이, 노쇼가 있어야 제한이 성립한다. Sprint 2에서 위험이 가장 크다 |
| **3. E5 관리자** | US-14 → US-13 → US-12 → US-15 | US-14·13은 1단계 메서드를 쓰고, US-12·15는 체크인 데이터가 있어야 의미가 있다 |
| **4. E1 보완** | US-03 (+C3·C5), US-16 | 토큰 무효화 인프라(C2) 위에 올린다 |
| **5. 통합·시연 준비** | 1.3의 실제 연동 통합 테스트, 핵심 시나리오 3건 리허설, Seed 갱신 | Backlog Success Metrics 확인 |

### 2.4 완료 기준 (Backlog Success Metrics)
- [ ] Must US 12개(Sprint 1 6개 + Sprint 2 6개)의 AC 100% 통과
- [ ] 핵심 시나리오 3건 데모 성공
  - (가) 가입 → **이메일 인증(실제 메일)** → 로그인 → 좌석 조회 → 예약 → QR 체크인
  - (나) 미체크인 예약 자동 취소 → 노쇼 3회 누적 → 이용 제한
  - (다) 관리자 시험 기간 설정 → 대시보드 확인 → 강제 취소
- [ ] 스케줄러 자동 취소·노쇼 카운터·이용 제한 전환/해제 단위 테스트(고정 Clock) 100% 통과
- [ ] 정책 값(예약 가능 기간·최대 시간·체크인 제한 시간·노쇼 기준·제한 기간)을 DB 값 변경만으로 바꾸는 시연
- [ ] 기존 기준 유지: 좌석 조회 500ms 이내, 동시 예약 20건 이중 배정 0건
- [ ] 1.2의 C1~C7 처리, `diff.md`의 남은 차이 갱신

### 2.5 위험과 대응
| 위험 | 대응 |
|---|---|
| 체크인과 스케줄러가 같은 예약을 동시에 처리 | 둘 다 `WHERE status='RESERVED'` 조건부 UPDATE, 반영 행 수로 판정. 동시성 테스트 추가 (설계서 Core Flow 2·3) |
| 노쇼 반영과 예약 생성 사이 교착 | 예약 생성은 User → 좌석, 노쇼·강제 취소는 예약 행 → User. 예약 생성이 예약 행을 잠그지 않으므로 교착이 없다는 설계를 테스트로 확인 |
| 학교 SMTP 계정·발송 제한 | 개발·테스트는 Mailpit, 시연용 SMTP는 일찍 확보. 실패 시 Mailpit 웹 화면으로 시연 |
| 체크인 단말기 시연 환경 | 키오스크 화면(`/kiosk`)을 브라우저 한 창으로 띄우고 단말기 키는 환경 변수로. 카메라가 없으면 QR 토큰 직접 입력으로 대체 |
| 공통 코드(`User`, `PolicyKey`, `ReservationService`) 동시 수정 충돌 | 1단계를 한 사람이 먼저 병합한 뒤 US별 브랜치를 나눈다 |

### 2.6 담당
Backlog에는 Sprint 2 담당이 정해져 있지 않다. 팀 회의에서 정해 아래를 채운다.

| US·항목 | 담당 |
|---|---|
| 1단계 공통 기반 | |
| US-09 · US-10 · US-11 | |
| US-12 · US-13 · US-14 · US-15 | |
| US-03 · US-16 · C1~C7 | |
