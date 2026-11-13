import EmptyState from '../common/EmptyState.jsx';
import { SEAT_STATUS_LABEL } from '../../constants/seatStatus.js';

/**
 * 좌석 그리드 (FR-R-01·02, US-05 AC1·2). gridRow·gridCol 위치에 상태별 색으로 표시한다.
 * 어떤 상태의 좌석이든 선택은 되며, 예약 가능 여부 안내는 ReservationPanel 이 맡는다 (AC2).
 *
 * @param {{
 *   seats: Array<{ seatId: number, seatNo: string, gridRow: number, gridCol: number, status: string }>,
 *   selectedSeatId?: number | null,
 *   onSelect?: (seat) => void,
 * }} props
 */
export default function SeatGrid({ seats, selectedSeatId, onSelect }) {
  if (seats.length === 0) return <EmptyState message="표시할 좌석이 없습니다." />;

  return (
    <div className="seat-grid" role="grid" aria-label="좌석 배치도">
      {seats.map((seat) => {
        const label = SEAT_STATUS_LABEL[seat.status] ?? seat.status;
        const selected = seat.seatId === selectedSeatId;
        return (
          <button
            key={seat.seatId}
            type="button"
            className={`seat seat--${seat.status.toLowerCase()}${selected ? ' seat--selected' : ''}`}
            style={{ gridRow: seat.gridRow, gridColumn: seat.gridCol }}
            title={`${seat.seatNo} · ${label}`}
            aria-label={`${seat.seatNo} ${label}`}
            aria-pressed={selected}
            onClick={() => onSelect?.(seat)}
          >
            {seat.seatNo}
          </button>
        );
      })}
    </div>
  );
}
