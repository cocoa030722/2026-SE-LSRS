/**
 * 취소 사유 선택지.
 * - 학생 직접 취소: FR-X-02 / US-08 AC3
 * - 관리자 강제 취소: FR-X-21 / US-14 AC2
 *
 * TODO(US-08, US-14): code 값은 가칭이다. 백엔드 enum이 확정되면 값을 맞춘다.
 */
export const STUDENT_CANCEL_REASONS = Object.freeze([
  { code: 'SCHEDULE_CHANGE', label: '일정 변경' },
  { code: 'SEAT_CHANGE', label: '좌석 위치 변경' },
  { code: 'TIME_CHANGE', label: '시간대 변경' },
  { code: 'OTHER', label: '기타' },
]);

export const ADMIN_CANCEL_REASONS = Object.freeze([
  { code: 'FACILITY_INSPECTION', label: '시설 긴급 점검' },
  { code: 'RULE_VIOLATION', label: '이용 규정 위반' },
  { code: 'SYSTEM_CORRECTION', label: '시스템 오류 보정' },
  { code: 'OTHER', label: '기타' },
]);
