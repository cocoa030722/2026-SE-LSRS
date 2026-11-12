import { useState } from 'react';
import PageHeader from '../../components/common/PageHeader.jsx';
import DashboardSummary from '../../components/admin/DashboardSummary.jsx';
import MiniSeatGrid from '../../components/admin/MiniSeatGrid.jsx';

/** 대시보드 자동 갱신 주기 (설계서 2장) */
const DASHBOARD_REFRESH_MS = 30_000;

/**
 * 실시간 이용 현황 대시보드 (US-12, Sprint 2)
 *
 * 데이터 흐름(예정): adminApi.getDashboard() 를 useAutoRefresh(fn, DASHBOARD_REFRESH_MS)로 주기 호출 (AC3)
 */
export default function DashboardPage() {
  // TODO(US-12): 위 데이터 흐름 구현
  const [dashboard] = useState(null);

  return (
    <>
      <PageHeader title="대시보드" stories={['US-12']} />
      <DashboardSummary summary={dashboard} />
      <MiniSeatGrid seats={dashboard?.seats ?? []} />
    </>
  );
}
