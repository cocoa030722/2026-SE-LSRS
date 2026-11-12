import { hourLabel, hourOf, normalizeSlot } from '../../utils/timeSlot.js';

/**
 * 날짜·시간대 선택 (FR-R-01, US-05 AC1·4, US-07 AC2).
 * 날짜는 정책의 예약 가능 기간으로 제한하고, 시각은 운영 시간 안의 정시만 고를 수 있다.
 * 종료 시각은 최대 이용 시간까지만 보여 준다.
 *
 * @param {{
 *   value: { date: string, start: string, end: string } | null,
 *   onChange: (value) => void,
 *   policy: { reservableFrom: string, reservableUntil: string, maxHours: number,
 *             openHour: number, closeHour: number } | null,
 * }} props
 */
export default function TimeSlotSelector({ value, onChange, policy }) {
  if (!value || !policy) return null;

  const start = hourOf(value.start);
  const startHours = range(policy.openHour, policy.closeHour - 1);
  const endHours = range(start + 1, Math.min(start + policy.maxHours, policy.closeHour));

  const change = (patch) => onChange(normalizeSlot({ ...value, ...patch }, policy));

  return (
    <div className="inline-form time-slot-selector">
      <label>
        날짜
        <input
          type="date"
          value={value.date}
          min={policy.reservableFrom}
          max={policy.reservableUntil}
          required
          onChange={(event) => event.target.value && change({ date: event.target.value })}
        />
      </label>
      <label>
        시작
        <select value={value.start} onChange={(event) => change({ start: event.target.value })}>
          {startHours.map((hour) => (
            <option key={hour} value={hourLabel(hour)}>
              {hourLabel(hour)}
            </option>
          ))}
        </select>
      </label>
      <label>
        종료
        <select value={value.end} onChange={(event) => change({ end: event.target.value })}>
          {endHours.map((hour) => (
            <option key={hour} value={hourLabel(hour)}>
              {hourLabel(hour)}
            </option>
          ))}
        </select>
      </label>
      <span className="hint">
        운영 시간 {hourLabel(policy.openHour)}~{hourLabel(policy.closeHour)}, 정시 1시간 단위
      </span>
    </div>
  );
}

function range(from, to) {
  const hours = [];
  for (let hour = from; hour <= to; hour += 1) hours.push(hour);
  return hours;
}
