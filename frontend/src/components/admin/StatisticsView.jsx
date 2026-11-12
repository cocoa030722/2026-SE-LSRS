import Placeholder from '../common/Placeholder.jsx';

/**
 * 날짜별·시간대별 가동률 (FR-A-03, US-15).
 * @param {{ statistics: object | null }} props GET /admin/statistics 응답
 */
export default function StatisticsView() {
  return (
    <Placeholder
      name="StatisticsView"
      story="US-15"
      todos={[
        '기간 선택 시 날짜별 가동률 (AC1)',
        '날짜 선택 시 시간대별 가동률 (AC2)',
        '데이터가 없으면 안내 (AC4)',
      ]}
    />
  );
}
