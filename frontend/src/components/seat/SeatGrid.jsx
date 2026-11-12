import Placeholder from '../common/Placeholder.jsx';

/**
 * 좌석 그리드 (FR-R-01·02, US-05).
 *
 * @param {{
 *   seats: Array<{ seatId: number, seatNo: string, gridRow: number, gridCol: number, status: string }>,
 *   selectedSeatId?: number | null,
 *   onSelect?: (seat) => void,
 * }} props
 */
export default function SeatGrid({ seats, selectedSeatId, onSelect }) {
  return (
    <Placeholder
      name="SeatGrid"
      story="US-05"
      todos={[
        'gridRow·gridCol 위치에 좌석 배치, 상태별 색상 표시 (AC1·2)',
        "'사용 중'·'예약됨' 좌석 선택 시 예약 진행 없이 안내 표시 (AC2)",
      ]}
    />
  );
}
