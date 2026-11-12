import Placeholder from '../common/Placeholder.jsx';

/**
 * 예약 상태 필터 (FR-R-21, US-08 AC2).
 * @param {{ value: string, onChange: (status: string) => void }} props 빈 문자열 = 전체
 */
export default function ReservationStatusFilter() {
  return (
    <Placeholder
      name="ReservationStatusFilter"
      story="US-08"
      todos={['전체 / 예약중 / 체크인 완료 / 취소됨 / 노쇼 선택 (RESERVATION_STATUS_LABEL)']}
    />
  );
}
