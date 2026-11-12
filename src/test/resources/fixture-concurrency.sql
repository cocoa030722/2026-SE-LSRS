-- 동시 예약 테스트용: 학생 20명(user_id 1~20), 좌석 20석(seat_id 1~20), 예약 없음
INSERT INTO users (user_id, login_id, email, password_hash, role, status, no_show_count, restricted_until, token_version)
WITH RECURSIVE n AS (SELECT 1 AS i UNION ALL SELECT i + 1 FROM n WHERE i < 20)
SELECT i, CONCAT('2026', LPAD(i, 4, '0')), CONCAT('s', i, '@test.ac.kr'), 'hash', 'STUDENT', 'ACTIVE', 0, NULL, 0
FROM n;

INSERT INTO seat (seat_id, seat_no, zone, grid_row, grid_col, active)
WITH RECURSIVE n AS (SELECT 1 AS i UNION ALL SELECT i + 1 FROM n WHERE i < 20)
SELECT i, CONCAT('A-', LPAD(i, 2, '0')), 'A', 1, i, TRUE
FROM n;
