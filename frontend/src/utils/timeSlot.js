/**
 * 시간대 선택 값 { date: 'YYYY-MM-DD', start: 'HH:00', end: 'HH:00' } 도우미 (US-05·06).
 * 예약은 운영 시간 안의 정시 1시간 단위다. 운영 종료 '24:00' 은 다음 날 00:00 이다.
 */

/** 13 → '13:00', 24 → '24:00' */
export function hourLabel(hour) {
  return `${String(hour).padStart(2, '0')}:00`;
}

/** '13:00' → 13 */
export function hourOf(time) {
  return Number.parseInt(time, 10);
}

/** 'YYYY-MM-DD' + n일 */
export function addDays(date, days) {
  const [year, month, day] = date.split('-').map(Number);
  const next = new Date(Date.UTC(year, month - 1, day + days));
  return next.toISOString().slice(0, 10);
}

/** 브라우저 현재 시각 기준 오늘 'YYYY-MM-DD' */
export function today() {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;
}

/** 날짜 + 'HH:00' → 서버 LocalDateTime 'YYYY-MM-DDTHH:00:00' ('24:00' 은 다음 날 00:00) */
export function toLocalDateTime(date, time) {
  const hour = hourOf(time);
  return hour === 24 ? `${addDays(date, 1)}T00:00:00` : `${date}T${hourLabel(hour)}:00`;
}

/**
 * 정책(운영 시간·최대 이용 시간)에 맞게 시간대를 보정한다.
 * 시작은 [open, close-1], 종료는 [시작+1, min(시작+maxHours, close)] 로 맞춘다.
 */
export function normalizeSlot(slot, policy) {
  if (!slot || !policy) return slot;
  const { openHour, closeHour, maxHours } = policy;
  const start = Math.min(Math.max(hourOf(slot.start), openHour), closeHour - 1);
  const latestEnd = Math.min(start + maxHours, closeHour);
  const end = Math.min(Math.max(hourOf(slot.end), start + 1), latestEnd);
  const next = { date: slot.date, start: hourLabel(start), end: hourLabel(end) };
  // 바뀐 것이 없으면 같은 객체를 돌려줘 불필요한 좌석 재조회를 막는다
  return next.start === slot.start && next.end === slot.end ? slot : next;
}

/** 첫 화면 기본값: 오늘 다음 정시부터 1시간. 오늘 운영이 끝났으면 다음 날 운영 시작부터. */
export function defaultSlot(policy) {
  const now = new Date();
  let date = policy.reservableFrom;
  let start = Math.max(now.getHours() + 1, policy.openHour);
  if (date !== today() || start >= policy.closeHour) {
    if (start >= policy.closeHour) date = addDays(date, 1);
    start = policy.openHour;
  }
  return { date, start: hourLabel(start), end: hourLabel(start + 1) };
}
