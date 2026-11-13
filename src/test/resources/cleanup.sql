-- 트랜잭션 롤백을 쓰지 않는 테스트(동시성)의 데이터 정리. 정책 파라미터(policy_setting)는 남긴다.
DELETE FROM reservation;
DELETE FROM exam_period;
DELETE FROM seat;
DELETE FROM users;
