import Placeholder from '../common/Placeholder.jsx';

/**
 * 날짜·시간대 선택 (FR-R-01, US-05 AC1·4, US-07 AC2).
 *
 * @param {{
 *   value: { date: string, start: string, end: string } | null,
 *   onChange: (value) => void,
 *   policy: object | null,
 * }} props
 */
export default function TimeSlotSelector({ value, onChange, policy }) {
  return (
    <Placeholder
      name="TimeSlotSelector"
      story="US-05"
      todos={[
        '날짜·시작·종료 시각 선택',
        '정책의 예약 가능 범위 밖 날짜는 선택 제한 또는 안내 (US-05 AC4)',
      ]}
    />
  );
}
