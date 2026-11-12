import { RESERVATION_STATUS_LABEL } from '../../constants/reservationStatus.js';

/**
 * 예약 상태 필터 (FR-R-21, US-08 AC2).
 * @param {{ value: string, onChange: (status: string) => void }} props 빈 문자열 = 전체
 */
export default function ReservationStatusFilter({ value, onChange }) {
  return (
    <label className="status-filter">
      상태
      <select value={value} onChange={(e) => onChange(e.target.value)}>
        <option value="">전체</option>
        {Object.entries(RESERVATION_STATUS_LABEL).map(([status, label]) => (
          <option key={status} value={status}>
            {label}
          </option>
        ))}
      </select>
    </label>
  );
}
