import EmptyState from '../common/EmptyState.jsx';
import { RESERVATION_STATUS_LABEL } from '../../constants/reservationStatus.js';
import { cancelReasonLabel } from '../../constants/cancelReasons.js';
import { formatTimeRange } from '../../utils/format.js';

/**
 * 예약 이력 목록 (FR-R-20, US-08 AC1). 서버가 최신순으로 내려준 순서를 그대로 보여 준다.
 * 취소 버튼은 서버가 cancellable=true 로 표시한 예약에만 노출한다 (AC3·5).
 *
 * @param {{
 *   reservations: Array<{ reservationId, seatNo, startAt, endAt, status, cancelReason, cancellable }>,
 *   emptyMessage: string,
 *   onCancel: (reservation) => void,
 * }} props
 */
export default function ReservationList({ reservations, emptyMessage, onCancel }) {
  if (reservations.length === 0) {
    return <EmptyState message={emptyMessage} />;
  }

  return (
    <table className="data-table">
      <thead>
        <tr>
          <th>좌석</th>
          <th>시간대</th>
          <th>상태</th>
          <th>취소 사유</th>
          <th aria-label="동작" />
        </tr>
      </thead>
      <tbody>
        {reservations.map((reservation) => (
          <tr key={reservation.reservationId}>
            <td>{reservation.seatNo}</td>
            <td>{formatTimeRange(reservation.startAt, reservation.endAt)}</td>
            <td>
              <span className={`status-badge status--${reservation.status.toLowerCase()}`}>
                {RESERVATION_STATUS_LABEL[reservation.status] ?? reservation.status}
              </span>
            </td>
            <td>{reservation.cancelReason ? cancelReasonLabel(reservation.cancelReason) : '-'}</td>
            <td>
              {reservation.cancellable && (
                <button type="button" onClick={() => onCancel(reservation)}>
                  취소
                </button>
              )}
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
