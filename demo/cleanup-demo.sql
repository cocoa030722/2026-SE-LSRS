-- =====================================================================
-- Sprint 1 데모 데이터 정리 (데모가 끝난 뒤 실행)
-- seed-demo.sql 이 넣은 데이터와 데모·리허설 중 생긴 데이터를 모두 지우고,
-- 앱을 처음 실행한 직후 상태(빈 테이블 + 정책 기본값)로 되돌린다.
--
--   지우는 것: 예약, 시험 기간, 이메일 인증 토큰, 계정(테스트 계정·데모 중 가입·탈퇴 계정), 좌석
--   되돌리는 것: 정책 파라미터 7개를 data.sql 기본값으로 (US-07 AC5 시연에서 바꾼 값 원복)
--   남기는 것: 테이블 구조
--
-- 주의: 위 테이블의 데이터를 모두 지운다. 데모 DB(로컬 compose MySQL)에서만 실행할 것.
-- 리허설 사이에 Seed 상태로 되돌릴 때는 이 파일이 아니라 seed-demo.sql 을 실행한다.
-- =====================================================================

-- FK 순서: 예약 → 시험 기간(등록 관리자 FK) → 인증 토큰 → 계정 → 좌석
DELETE FROM reservation;
DELETE FROM exam_period;
DELETE FROM verification_tokens;
DELETE FROM users;
DELETE FROM seat;
ALTER TABLE reservation AUTO_INCREMENT = 1;
ALTER TABLE exam_period AUTO_INCREMENT = 1;
ALTER TABLE verification_tokens AUTO_INCREMENT = 1;
ALTER TABLE users AUTO_INCREMENT = 1;
ALTER TABLE seat AUTO_INCREMENT = 1;

-- 정책 파라미터 기본값 (src/main/resources/data.sql 과 같은 값)
UPDATE policy_setting SET policy_value = '7',  updated_at = NOW() WHERE policy_key = 'NORMAL_DAYS';
UPDATE policy_setting SET policy_value = '4',  updated_at = NOW() WHERE policy_key = 'NORMAL_MAX_HOURS';
UPDATE policy_setting SET policy_value = '1',  updated_at = NOW() WHERE policy_key = 'EXAM_DAYS';
UPDATE policy_setting SET policy_value = '3',  updated_at = NOW() WHERE policy_key = 'EXAM_MAX_HOURS';
UPDATE policy_setting SET policy_value = '15', updated_at = NOW() WHERE policy_key = 'CHECKIN_WINDOW_MIN';
UPDATE policy_setting SET policy_value = '5',  updated_at = NOW() WHERE policy_key = 'OPEN_HOUR';
UPDATE policy_setting SET policy_value = '24', updated_at = NOW() WHERE policy_key = 'CLOSE_HOUR';

-- 결과 확인 (모두 0, 정책 7개)
SELECT
  (SELECT COUNT(*) FROM reservation)         AS reservation,
  (SELECT COUNT(*) FROM exam_period)         AS exam_period,
  (SELECT COUNT(*) FROM verification_tokens) AS verification_tokens,
  (SELECT COUNT(*) FROM users)               AS users,
  (SELECT COUNT(*) FROM seat)                AS seat,
  (SELECT COUNT(*) FROM policy_setting)      AS policy_setting;
