import Placeholder from '../common/Placeholder.jsx';

/**
 * 현재 이용 인원·잔여 좌석·가동률 (FR-A-01, US-12 AC1).
 * @param {{ summary: { inUseCount: number, availableCount: number, utilizationRate: number } | null }} props
 */
export default function DashboardSummary() {
  return (
    <Placeholder
      name="DashboardSummary"
      story="US-12"
      todos={['현재 이용 인원, 잔여 좌석 수, 가동률 카드']}
    />
  );
}
