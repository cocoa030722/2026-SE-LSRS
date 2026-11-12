import Placeholder from '../common/Placeholder.jsx';

/**
 * 현재 적용 중인 예약 정책 안내 (FR-R-03, US-07 AC3).
 * 시험 기간이면 최대 예약 가능 시간(3시간)을 명시한다.
 *
 * @param {{ policy: object | null }} props GET /policies/current 응답
 */
export default function PolicyNotice({ policy }) {
  return (
    <Placeholder
      name="PolicyNotice"
      story="US-07"
      todos={['시험 기간 여부, 예약 가능 기간, 최대 이용 시간 표시']}
    />
  );
}
