-- =====================================================================
-- Sprint 1 데모용 Seed 데이터 (데모 직전에 실행, 여러 번 실행해도 같은 상태로 초기화됨)
-- 주의: users·seat·reservation·exam_period·verification_tokens 의 기존 데이터를 모두 지운다.
--       데모 DB(로컬 compose MySQL)에서만 실행할 것.
-- 모든 계정 비밀번호: Passw0rd!   (BCrypt 해시)
-- 날짜는 실행 시점(CURDATE/NOW) 기준으로 계산되므로 데모 당일 아침에 다시 실행한다.
-- =====================================================================
SET @pw = '$2a$10$YSvMNgV/M66S8QEvip0JF.B8tlkDS05oDooLjPyHlNrmbgrB5rQOW';

DELETE FROM reservation;
DELETE FROM exam_period;
DELETE FROM verification_tokens;
DELETE FROM users;
DELETE FROM seat;
ALTER TABLE users AUTO_INCREMENT = 1;
ALTER TABLE seat AUTO_INCREMENT = 1;
ALTER TABLE reservation AUTO_INCREMENT = 1;
ALTER TABLE exam_period AUTO_INCREMENT = 1;

-- 정책 파라미터를 기본값으로 되돌린다 (US-07 AC5 시연 후 원복용)
UPDATE policy_setting SET policy_value = '7'  WHERE policy_key = 'NORMAL_DAYS';
UPDATE policy_setting SET policy_value = '4'  WHERE policy_key = 'NORMAL_MAX_HOURS';
UPDATE policy_setting SET policy_value = '1'  WHERE policy_key = 'EXAM_DAYS';
UPDATE policy_setting SET policy_value = '3'  WHERE policy_key = 'EXAM_MAX_HOURS';
UPDATE policy_setting SET policy_value = '15' WHERE policy_key = 'CHECKIN_WINDOW_MIN';

-- 좌석 20석: 4행 × 5열 (A-01 ~ D-05)
INSERT INTO seat (seat_no, zone, grid_row, grid_col, active) VALUES
 ('A-01','A',1,1,1),('A-02','A',1,2,1),('A-03','A',1,3,1),('A-04','A',1,4,1),('A-05','A',1,5,1),
 ('B-01','B',2,1,1),('B-02','B',2,2,1),('B-03','B',2,3,1),('B-04','B',2,4,1),('B-05','B',2,5,1),
 ('C-01','C',3,1,1),('C-02','C',3,2,1),('C-03','C',3,3,1),('C-04','C',3,4,1),('C-05','C',3,5,1),
 ('D-01','D',4,1,1),('D-02','D',4,2,1),('D-03','D',4,3,1),('D-04','D',4,4,1),('D-05','D',4,5,1);

-- 테스트 계정 (user_id 1~6)
INSERT INTO users (login_id, email, password_hash, role, status, no_show_count, restricted_until, token_version) VALUES
 ('admin',    'admin@univ.ac.kr',    @pw, 'ADMIN',   'ACTIVE',  0, NULL, 1),                          -- 1 관리자 (US-02 AC4, US-07)
 ('20260001', 'student1@univ.ac.kr', @pw, 'STUDENT', 'ACTIVE',  1, NULL, 1),                          -- 2 학생A: 메인 시연 (이력 보유)
 ('20260002', 'student2@univ.ac.kr', @pw, 'STUDENT', 'ACTIVE',  0, NULL, 1),                          -- 3 학생B: 다른 학생 (예약됨·사용 중 좌석)
 ('20260003', 'student3@univ.ac.kr', @pw, 'STUDENT', 'ACTIVE',  3, NOW() + INTERVAL 3 DAY, 1),        -- 4 학생C: 이용 제한 중 (US-06 AC3, US-04 AC2)
 ('20260004', 'student4@univ.ac.kr', @pw, 'STUDENT', 'ACTIVE',  0, NULL, 1),                          -- 5 학생D: 탈퇴 성공용 (US-04 AC1·3)
 ('20260005', 'student5@univ.ac.kr', @pw, 'STUDENT', 'PENDING', 0, NULL, 1);                          -- 6 학생E: 이메일 미인증 (US-02 AC2)

-- 예약 데이터
SET @tmr = CURDATE() + INTERVAL 1 DAY;
SET @hr  = DATE_FORMAT(NOW(), '%Y-%m-%d %H:00:00');
INSERT INTO reservation (user_id, seat_id, start_at, end_at, status, cancel_reason, canceled_by, checked_in_at, penalty_applied, created_at) VALUES
 -- 학생B: 내일 10~12시 A-02 예약 → 학생A 화면에서 '예약됨' (US-05 AC2, US-06 AC4)
 (3, 2, @tmr + INTERVAL 10 HOUR, @tmr + INTERVAL 12 HOUR, 'RESERVED',   NULL, NULL, NULL, 0, NOW() - INTERVAL 1 HOUR),
 -- 학생B: 지금 시간대 A-03 체크인 → 오늘 현재 시간대 조회 시 '사용 중' (US-05 AC2)
 (3, 3, @hr, @hr + INTERVAL 2 HOUR, 'CHECKED_IN', NULL, NULL, @hr + INTERVAL 5 MINUTE, 0, NOW() - INTERVAL 1 DAY),
 -- 학생A 이력: 체크인 완료 / 취소됨 / 노쇼(자동 취소) (US-08 AC1·2·5)
 (2, 6, CURDATE() - INTERVAL 3 DAY + INTERVAL 9 HOUR,  CURDATE() - INTERVAL 3 DAY + INTERVAL 12 HOUR, 'CHECKED_IN',     NULL, NULL, CURDATE() - INTERVAL 3 DAY + INTERVAL 9 HOUR + INTERVAL 3 MINUTE, 0, NOW() - INTERVAL 4 DAY),
 (2, 7, CURDATE() - INTERVAL 2 DAY + INTERVAL 13 HOUR, CURDATE() - INTERVAL 2 DAY + INTERVAL 15 HOUR, 'CANCELLED',      'SCHEDULE_CHANGE', 'STUDENT', NULL, 0, NOW() - INTERVAL 3 DAY),
 (2, 8, CURDATE() - INTERVAL 1 DAY + INTERVAL 18 HOUR, CURDATE() - INTERVAL 1 DAY + INTERVAL 20 HOUR, 'AUTO_CANCELLED', NULL, 'SYSTEM', NULL, 1, NOW() - INTERVAL 2 DAY),
 -- 학생C: 진행 중 예약 → 탈퇴 거부 (US-04 AC2)
 (4, 16, @tmr + INTERVAL 14 HOUR, @tmr + INTERVAL 16 HOUR, 'RESERVED', NULL, NULL, NULL, 0, NOW() - INTERVAL 5 DAY);
