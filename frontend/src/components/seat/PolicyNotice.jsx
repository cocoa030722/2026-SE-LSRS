import ErrorMessage from '../common/ErrorMessage.jsx';

/**
 * 현재 적용 중인 예약 정책 안내 (FR-R-03, US-07 AC3).
 * 시험 기간이면 최대 예약 가능 시간(3시간)을 강조해 명시한다.
 *
 * @param {{
 *   policy: { examPeriod: boolean, reservableFrom: string, reservableUntil: string, maxHours: number } | null,
 *   error: { message: string } | null,
 * }} props GET /policies/current 응답
 */
export default function PolicyNotice({ policy, error }) {
  if (error) return <ErrorMessage error={error} />;
  if (!policy) return null;

  return (
    <section className={`policy-notice${policy.examPeriod ? ' policy-notice--exam' : ''}`}>
      <strong>{policy.examPeriod ? '시험 기간 정책 적용 중' : '평상시 정책'}</strong>
      <span>
        예약 가능 기간 {policy.reservableFrom} ~ {policy.reservableUntil}
      </span>
      <span className="policy-max-hours">최대 이용 시간 {policy.maxHours}시간</span>
    </section>
  );
}
