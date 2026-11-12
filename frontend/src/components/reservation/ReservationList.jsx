import Placeholder from '../common/Placeholder.jsx';

/**
 * 예약 이력 목록 (FR-R-20, US-08 AC1).
 *
 * @param {{
 *   reservations: Array<object>,
 *   onCancel: (reservation) => void,
 *   onOpenDetail: (reservation) => void,
 * }} props
 */
export default function ReservationList({ reservations, onCancel, onOpenDetail }) {
  return (
    <Placeholder
      name="ReservationList"
      story="US-08"
      todos={[
        '좌석·시간대·상태를 최신순으로 표시, 이력이 없으면 안내 문구 (AC1)',
        '취소 버튼은 취소 가능한 예약에만 노출 (AC3·5)',
      ]}
    />
  );
}
