-- 정책 파라미터 기본값 (명세서 FR-A-20, FR-C-02 / 설계서 3장 PolicySetting).
-- 이미 있는 키는 덮어쓰지 않는다(관리자가 바꾼 값을 유지). 값 변경은 PUT /api/admin/policies/{key} 또는 DB에서 한다.
-- 운영 시간(OPEN_HOUR·CLOSE_HOUR)은 중앙도서관 운영 시간 05:00 ~ 24:00 이다.
INSERT IGNORE INTO policy_setting (policy_key, policy_value, updated_at) VALUES
  ('NORMAL_DAYS', '7', NOW()),
  ('NORMAL_MAX_HOURS', '4', NOW()),
  ('EXAM_DAYS', '1', NOW()),
  ('EXAM_MAX_HOURS', '3', NOW()),
  ('CHECKIN_WINDOW_MIN', '15', NOW()),
  ('OPEN_HOUR', '5', NOW()),
  ('CLOSE_HOUR', '24', NOW());
