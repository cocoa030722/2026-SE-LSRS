import { SEAT_STATUS_LABEL } from '../../constants/seatStatus.js';

/** 좌석 상태 범례 (FR-R-02, US-05 AC2) */
export default function SeatLegend() {
  return (
    <ul className="seat-legend">
      {Object.entries(SEAT_STATUS_LABEL).map(([status, label]) => (
        <li key={status}>
          <span className={`seat-swatch seat--${status.toLowerCase()}`} aria-hidden="true" />
          {label}
        </li>
      ))}
    </ul>
  );
}
