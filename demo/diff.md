# Backlog·설계서 대비 현행 구현의 차이와 이유

- 작성일: 2026-11-16
- 현행 기준: 브랜치 `fix/build-and-tests` (`daa3c53`)
- 비교 대상
  - `docs/제출한 문서/Product_Backlog_final.docx` (v1.0, 2026-10-22): User Story·AC
  - `docs/제출한 문서/System_Architecture_Design_final.docx` (v1.1, 2026-11-05): API·데이터·보안·개발 환경
  - `docs/기술스택_최신버전.md`: 버전은 설계서보다 이 문서가 우선이다 (CLAUDE.md)
- 범위: Sprint 1 US(US-01·02·04·05·06·07·08). Sprint 2 US는 설계대로 아직 구현하지 않았으므로 차이로 보지 않는다. 미구현 목록은 [`sprint2.md`](sprint2.md)에 있다.
- **이유 표기:** 커밋·코드 주석·데모 문서에 근거가 있으면 그대로 적었다. 근거가 없어 코드로 추정한 이유에는 **(추정)** 을 붙였다. 발표 전에 담당자가 확인한다.

---

## 0. 요약 (PPT 한 장)

| 구분 | 내용 |
|---|---|
| US 단위 | Sprint 1 US 7개를 모두 구현했다. US를 통째로 미룬 것은 없다 |
| AC가 달라진 곳 (3건) | ① US-01 AC1: 인증 메일을 보내지 않고 서버 콘솔에 출력한다 ② US-02 AC1: Refresh 토큰(7일)이 없다 ③ US-02 AC3: 서버 로그아웃 API가 없다 (브라우저 토큰 삭제) → 모두 **Sprint 2로 이월** |
| 설계 세부가 달라진 곳 | 이메일 인증 토큰 구조·API 형태, 학생 전용 API의 서버 권한 검사, 다른 학생 예약 접근 시 404, 관리자 정책 조회 API 추가, `?date=` 파라미터 추가 |
| 버전 | 설계서(Java 21·Boot 3.3·React 18 등) 대신 `기술스택_최신버전.md`(Boot 4.1.2·React 19 등)를 따랐다. 예외: Java toolchain 21, jjwt 0.11.5, Gradle 9.8.2 |
| 이유 | 이메일 발송 인프라와 Refresh 토큰은 같은 E1 인프라를 쓰는 US-03(Sprint 2)과 함께 만든다. 일정 안에 Sprint 1 핵심 흐름(가입 → 로그인 → 조회 → 예약 → 취소)을 끊김 없이 시연하는 것을 우선했다 |

---

## 1. Backlog AC와 다른 점

| US·AC | Backlog AC | 현행 | 이유 | 후속 |
|---|---|---|---|---|
| **US-01 AC1** | 가입 시 학교 이메일로 인증 링크(또는 6자리 코드)가 **발송**된다 | 메일을 보내지 않는다. 인증 토큰(UUID)을 서버 콘솔에 `이메일 인증 토큰: …`으로 출력하고, 화면에서 그 토큰으로 인증한다 (`AuthService.signup`) | 메일 발송(MailSender, Mailpit, 학교 SMTP)은 비밀번호 재설정 메일(US-03, Sprint 2)과 같은 인프라다. Sprint 1에서는 가입 → PENDING → 인증 → ACTIVE 흐름과 상태 전이를 먼저 완성했다. 시연에서는 콘솔 출력으로 같은 흐름을 보여 준다 | Sprint 2: MailSender + Mailpit(개발)·학교 SMTP(시연) |
| **US-02 AC1** | JWT **Access 1시간, Refresh 7일**이 발급된다 | Access 토큰(1시간)만 발급한다. Refresh 토큰·재발급 API가 없다 | Access 1시간은 `f7edd8a`에서 맞췄다(이전 30분). Refresh 토큰은 저장·폐기 테이블(RefreshToken)과 재발급 API가 필요하고, 폐기 시점이 로그아웃·비밀번호 변경(US-03)·권한 변경(US-16)과 묶여 있어 Sprint 2에서 함께 만든다 | Sprint 2 |
| **US-02 AC3** | **로그아웃한 상태**로 보호된 API에 접근하면 401 | 로그아웃은 브라우저의 토큰 삭제로 처리한다. 서버 로그아웃 API가 없어, 로그아웃 전에 복사해 둔 토큰은 만료(1시간)까지 유효하다. 토큰 없이 접근하면 401 → 로그인 화면은 동작한다 | 설계서의 서버 로그아웃은 "Refresh 폐기"다. Refresh가 없으므로 함께 미뤘다. 명세서 FR-U-14도 "클라이언트 측 토큰을 무효화"라 현행이 명세서 요구는 만족한다. 탈퇴는 `tokenVersion` 증가로 기존 토큰이 즉시 401이 된다 | Sprint 2: `POST /api/auth/logout` (Refresh 폐기) |

> 위 3건 외의 AC(US-01 AC2~5, US-02 AC1의 1시간·AC2·4·5, US-04 AC1~3, US-05~08 전체)는 Backlog대로 동작한다. 화면 시연 37개 점검과 자동 테스트로 확인했다 (`demo/스프린트1_데모_준비_및_실행.md` 0장).
>
> Backlog의 "Sprint 간 의존성 처리"대로, Sprint 2 기능이 있어야 생기는 상태(이용 제한 학생, 체크인 완료·노쇼 예약, 노쇼 카운터)는 Seed 데이터(`demo/seed-demo.sql`)로 검증했다. 이것은 계획된 방식이므로 차이가 아니다.

---

## 2. 설계서와 다른 점

### 2.1 API (설계서 4장)

| 항목 | 설계서 | 현행 | 이유 |
|---|---|---|---|
| 이메일 인증 | `POST /api/auth/verify-email` (코드 확인) | `GET /api/auth/verify-email?token=` | 메일의 **링크를 누르면 바로 인증되는** 방식을 택했다. 링크는 GET 요청이므로 GET으로 받는다. Backlog AC1은 "링크 또는 6자리 코드"를 모두 허용한다 **(추정)** |
| `POST /api/auth/token/refresh`, `POST /api/auth/logout` | 있음 | 없음 | 1장 US-02 AC1·AC3과 같다. Sprint 2 |
| `GET /api/admin/policies` | 목록에 없음 (`PUT /api/admin/policies/{key}`만 있음) | **추가** | 관리자 정책 화면에 현재 값과 변경 시각을 보여 줘야 수정할 수 있다. 조회 없이 수정만 있으면 화면을 만들 수 없다 |
| `GET /api/policies/current` | 파라미터 없음 | `?date=` 선택 파라미터 **추가** (생략하면 오늘) | 설계서 3장 규칙 "오늘 **또는 예약 대상일**이 시험 기간이면 시험 정책"을 화면에서 보여 주려면 대상일이 필요하다. 시험 기간 직전에 시험 기간 날짜를 고르면 그 날짜의 정책(최대 3시간)이 보여야 한다 (`PolicyController` 주석) |
| `GET /api/reservations/{id}` (상세 + QR) | US-08·09 | 없음 | QR(US-09)과 한 화면이라 Sprint 2에 함께 만든다. Sprint 1의 US-08 AC는 목록 API(`/me`)만으로 충족된다 (`ReservationController` 주석) |
| 학생 전용 API의 권한 | `/api/seats`, `/api/reservations/**`, `DELETE /api/users/me`는 **Student** | 서버는 "로그인한 사용자"면 허용한다. `/api/admin/**`만 ADMIN을 검사한다. 화면 라우팅(`RequireAuth roles=[STUDENT]`)에서는 학생만 들어간다 | Sprint 1에는 관리자가 학생 API를 부를 화면이 없어 `SecurityConfig`를 "관리자 / 그 외 인증" 두 단계로만 나눴다 **(추정)**. 관리자 토큰으로 API를 직접 호출하면 예약이 생성될 수 있다 → Sprint 2에서 `hasRole("STUDENT")`를 추가하고 403 테스트를 붙인다 |
| 다른 학생의 예약 취소 (US-02 AC5) | 오류 규약상 "403 권한 없음" | **404** "예약을 찾을 수 없습니다." | AC5의 "B의 예약 정보는 노출되지 않는다"를 지키려고 했다. 403을 주면 "그 번호의 예약이 존재한다"는 사실이 드러난다. 본인 예약 조회(`findByReservationIdAndUserUserId`)에서 걸러 없는 예약과 같은 응답을 준다 (`demo/US-07_US-08_작동원리.md`) |

### 2.2 데이터 모델 (설계서 3장)

| 엔티티 | 설계서 | 현행 | 이유 |
|---|---|---|---|
| VerificationToken | `tokenId, userId(FK), type(EMAIL_VERIFY/PASSWORD_RESET), tokenHash(UK), expiresAt, usedAt` | `id, email, token(평문, UK), expiryDate`. 사용하면 **행을 삭제**한다(`deleteByEmail`). 유효 기간 24시간 | Sprint 1에는 이메일 인증 한 종류뿐이라 `type`이 필요 없고, 사용 후 삭제해도 "이미 사용된 링크는 거부"(US-01 AC5)를 만족한다 **(추정)**. 토큰 평문 저장과 email 기준 연결은 설계보다 약하다 → US-03(재설정 토큰)을 만들 때 설계서 구조(userId·type·tokenHash·usedAt)로 바꾼다 |
| RefreshToken | 있음 | 없음 | 1장 US-02 AC1. Sprint 2 |
| User | `addNoShow()`, `release()` | 없음. `isRestricted(now)`, `activate()`, `withdraw()`만 있다 | 노쇼 반영·해제는 US-10·11·13(Sprint 2)에서 쓴다. Sprint 1의 이용 제한 판정(US-06 AC3, US-04 AC2)은 `restrictedUntil` 값만 읽으면 된다 (`User` 주석) |
| PolicySetting 키 | `NORMAL_DAYS, NORMAL_MAX_HOURS, EXAM_DAYS, EXAM_MAX_HOURS, CHECKIN_WINDOW_MIN, NOSHOW_THRESHOLD, RESTRICTION_DAYS, OPEN_HOUR, CLOSE_HOUR` | `NOSHOW_THRESHOLD`, `RESTRICTION_DAYS` 없음 | 노쇼 기준·제한 기간은 US-11(Sprint 2)에서만 쓴다. 각 기능 담당이 추가하기로 했다 (`PolicyKey` 주석) |
| CancelReason | 학생 사유 + 관리자 사유(시설 긴급 점검·이용 규정 위반·시스템 오류 보정·기타) | 학생 사유 4개만 | 관리자 강제 취소(US-14)는 Sprint 2 (`CancelReason` 주석) |
| User, Seat, Reservation, ExamPeriod, 인덱스 3개 | — | 설계서와 같다 | — |

### 2.3 아키텍처·보안

| 항목 | 설계서 | 현행 | 이유 |
|---|---|---|---|
| 현재 시각 | "현재 시각은 주입된 Clock으로만 얻음" (NFR 4.2) | 예약·정책·좌석·탈퇴는 `Clock`을 쓴다. **이메일 인증 토큰 생성·만료 판정**(`AuthService`, `VerificationToken.isExpired`)만 `LocalDateTime.now()`를 직접 쓴다 | US-01은 다른 팀원이 먼저 작성해 병합한 코드이고, 만료 시간을 조작하는 테스트가 Sprint 1 AC에 없었다 **(추정)**. US-03에서 토큰을 재설계할 때 `Clock`으로 바꾼다 |
| MailSender | Infra 계층에 있음 (테스트 시 Mock) | 없음 (`spring-boot-starter-mail` 미도입) | 1장 US-01 AC1 |
| JWT 비밀키 | 명시 없음 (설정값) | `JWT_SECRET` 환경 변수가 없으면 `application.properties`의 **데모용 기본 키**를 쓴다 (`0aeb98f`) | 데모 PC에서 환경 변수 설정을 빠뜨려 서버가 뜨지 않는 위험을 없애려고 넣었다. **데모가 끝나면 되돌린다** (보안 부채) |
| Swagger UI (springdoc) | 개발 도구에 포함 | 미도입 | Sprint 1에는 화면과 Postman·curl로 API를 확인했다 **(추정)** |
| 학생 직접 취소 가능 범위 | '체크인 대기'(시작 ≤ now ≤ 시작+15분)까지 취소 가능 | 같다. 상태를 저장하지 않고 `now - CHECKIN_WINDOW_MIN`으로 계산한다 | 차이 아님 (확인용) |

### 2.4 기술 스택·개발 환경 (설계서 5장)

설계서 v1.1의 버전은 `기술스택_최신버전.md`로 대체되었다(CLAUDE.md). 아래는 세 문서를 함께 비교한 것이다. **굵은 칸**이 기술스택 문서와 다른 곳이다.

| 항목 | 설계서 v1.1 | 기술스택_최신버전.md | 현행 | 이유 |
|---|---|---|---|---|
| Java | 21 (LTS) | 27.0.1 | **toolchain 21** (`build.gradle`) | 확인 필요 **(추정: 팀원 개발 환경의 JDK 21과 LTS 기준을 유지)**. 맞추려면 `languageVersion = 27`로 바꾸고 전체 테스트를 다시 돌린다 |
| Spring Boot | 3.3 | 4.1.2 | 4.1.2 | 기술스택 문서 기준 |
| jjwt | 0.12 | 0.13.0 | **0.11.5** | US-02 코드가 0.11 API(`parserBuilder()`, `setSubject()`, `SignatureAlgorithm`)로 작성되었다. 0.12부터 이 API가 바뀌어(`parser().verifyWith()` 등) 버전만 올리면 컴파일 경고·코드 수정이 필요하다 **(추정)**. Sprint 2에서 Refresh 토큰을 만들 때 함께 올린다 |
| Gradle | 8 | 9.9.1 | **9.8.2** (wrapper) | wrapper 생성 시점 버전 **(추정)**. `gradlew wrapper --gradle-version 9.9.1`로 맞춘다 |
| Node.js / npm | 20 LTS | 26.13.1 / 12.2.2 | 26.13.1 (`.nvmrc`, `engines`) | — |
| React / Vite / React Router | 18 / 5 / 6 | 19.3.1 / 8.3.8 / 8.5.1 | 같음 | — |
| Axios / qrcode.react / html5-qrcode | 버전 없음 | 1.21.1 / 4.2.0 / 2.3.8 | 같음 (QR 두 개는 설치만, 사용은 Sprint 2) | — |
| MySQL | 8.0 | 26.10.0 | 26.10.0 (`compose.yaml`, Testcontainers) | — |
| JUnit / Mockito | 5 / — | 6.1.3 / 5.24.0 | 같음 (`build.gradle`에서 고정) | — |
| Testcontainers | MySQL 8 | 2.0.5 | Spring Boot 관리 버전 (고정 안 함) | Boot 4.1.2가 관리하는 버전을 그대로 쓴다 |
| Mailpit | 사용 | 1.33.1 | **미도입** (`compose.yaml`에 없음) | 1장 US-01 AC1 |
| springdoc-openapi | 사용 | 3.2.0 | **미도입** | 2.3 참고 |

---

## 3. 설계서에 없던 값·규칙을 정한 부분 (빈칸 보완)

설계서와 충돌하지 않고, 설계서가 정하지 않은 값을 구현하면서 정한 것이다. 질문을 받으면 근거를 설명한다.

| 항목 | 정한 값·규칙 | 근거 |
|---|---|---|
| 운영 시간 | `OPEN_HOUR=5`, `CLOSE_HOUR=24` (05:00~24:00) | 설계서는 키 이름만 있다. 중앙도서관 운영 시간으로 정했다 (`data.sql` 주석). DB 값이므로 바꿀 수 있다 |
| 이메일 인증 토큰 유효 시간 | 24시간 | 설계서·Backlog에 값이 없다 |
| 좌석 상태 우선순위 | 한 좌석에 겹치는 예약이 여럿이면 내 예약 > 사용 중 > 예약됨 | 조회 구간이 여러 예약에 걸칠 때 한 상태만 표시해야 한다 (`SeatStatus` 주석) |
| 이미 시작된 시간대 | 예약 거부 ("이미 시작된 시간대는 예약할 수 없습니다.") | 지난 시간을 예약하면 바로 노쇼 대상이 된다 |
| 정책 값 범위 | 키마다 최솟값·최댓값 검사 (예: `OPEN_HOUR` 0~23, `CLOSE_HOUR` 1~24, 최대 시간 1 이상) | 잘못된 값으로 예약이 전부 막히는 것을 방지한다 (`PolicyKey`) |
| 예약 목록의 취소 가능 여부 | 응답에 `cancellable`을 넣어 화면이 취소 버튼을 보일지 정한다 | 취소 가능 판단(체크인 가능 시간 계산)을 화면에 중복하지 않으려고 서버가 계산한다 |
| 탈퇴 비식별화 값 | `loginId = WITHDRAWN_{id}`, `email = WITHDRAWN_{id}@deleted.com` | 설계서의 "loginId·email 비식별화"를 UNIQUE 제약을 지키는 값으로 구체화했다 |
