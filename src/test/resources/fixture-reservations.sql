-- 사용자 1(본인), 2(다른 학생) / 좌석 2개 / 예약 5건 / 시험 기간 1건
INSERT INTO users (user_id, login_id, email, password_hash, role, status, no_show_count, restricted_until, token_version) VALUES
  (1, '20260001', 's1@test.ac.kr', 'hash', 'STUDENT', 'ACTIVE', 0, NULL, 0),
  (2, '20260002', 's2@test.ac.kr', 'hash', 'STUDENT', 'ACTIVE', 0, NULL, 0);

INSERT INTO seat (seat_id, seat_no, zone, grid_row, grid_col, active) VALUES
  (1, 'A-01', 'A', 1, 1, TRUE),
  (2, 'A-02', 'A', 1, 2, TRUE);

INSERT INTO reservation (reservation_id, user_id, seat_id, start_at, end_at, status, checked_in_at, cancel_reason, canceled_by, penalty_applied, created_at) VALUES
  (10, 1, 1, '2026-12-01 09:00:00', '2026-12-01 11:00:00', 'CHECKED_IN', '2026-12-01 09:05:00', NULL, NULL, FALSE, '2026-11-28 10:00:00'),
  (11, 1, 2, '2026-12-02 13:00:00', '2026-12-02 15:00:00', 'RESERVED', NULL, NULL, NULL, FALSE, '2026-11-28 10:00:00'),
  (12, 1, 1, '2026-11-30 09:00:00', '2026-11-30 10:00:00', 'CANCELLED', NULL, 'OTHER', 'STUDENT', FALSE, '2026-11-28 10:00:00'),
  (13, 2, 2, '2026-12-03 09:00:00', '2026-12-03 10:00:00', 'RESERVED', NULL, NULL, NULL, FALSE, '2026-11-28 10:00:00'),
  (14, 1, 1, '2026-11-29 09:00:00', '2026-11-29 10:00:00', 'AUTO_CANCELLED', NULL, NULL, 'SYSTEM', TRUE, '2026-11-28 10:00:00');

INSERT INTO exam_period (exam_period_id, start_date, end_date, created_by) VALUES
  (1, '2026-12-14', '2026-12-20', 1);
