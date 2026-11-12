import { useState } from 'react';
import PageHeader from '../../components/common/PageHeader.jsx';
import StatisticsView from '../../components/admin/StatisticsView.jsx';

/**
 * 좌석 사용 통계 (US-15, Sprint 2)
 * 데이터 흐름(예정): adminApi.getStatistics({ from, to })
 */
export default function StatisticsPage() {
  // TODO(US-15): 기간 선택 UI와 조회 구현
  const [statistics] = useState(null);

  return (
    <>
      <PageHeader title="사용 통계" stories={['US-15']} />
      <StatisticsView statistics={statistics} />
    </>
  );
}
