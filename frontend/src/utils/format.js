/** "2026-12-01T13:00:00" 형식(서버 LocalDateTime)을 화면 표시용으로 바꾼다. */
function splitDateTime(value) {
  const [date, time = ''] = String(value).split('T');
  return { date, time: time.slice(0, 5) };
}

/** 예: "2026-12-01 13:00 ~ 15:00" (날짜가 다르면 종료 날짜도 표시) */
export function formatTimeRange(startAt, endAt) {
  const start = splitDateTime(startAt);
  const end = splitDateTime(endAt);
  const endText = start.date === end.date ? end.time : `${end.date} ${end.time}`;
  return `${start.date} ${start.time} ~ ${endText}`;
}
