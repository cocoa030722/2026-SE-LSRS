import { useState } from 'react';
import PageHeader from '../../components/common/PageHeader.jsx';
import ReservationStatusFilter from '../../components/reservation/ReservationStatusFilter.jsx';
import ReservationList from '../../components/reservation/ReservationList.jsx';
import CancelReasonDialog from '../../components/reservation/CancelReasonDialog.jsx';

/**
 * 내 예약 조회 및 직접 취소 (US-08)
 *
 * 데이터 흐름(예정)
 *  1. status 변경 시 reservationApi.getMine({ status }) → reservations (AC1·2)
 *  2. 취소 → CancelReasonDialog 에서 사유 선택 → reservationApi.cancel(id, { reason }) → 목록 갱신 (AC3)
 *  3. 서버 거부(이미 종결된 예약·타인 예약)는 ErrorMessage 로 안내 (AC5)
 */
export default function MyReservationsPage() {
  // TODO(US-08): 위 데이터 흐름 구현
  const [status, setStatus] = useState('');
  const [reservations] = useState([]);
  const [cancelTarget, setCancelTarget] = useState(null);

  return (
    <>
      <PageHeader title="내 예약" stories={['US-08']}>
        <ReservationStatusFilter value={status} onChange={setStatus} />
      </PageHeader>
      <ReservationList reservations={reservations} onCancel={setCancelTarget} onOpenDetail={() => {}} />
      <CancelReasonDialog
        open={cancelTarget !== null}
        onClose={() => setCancelTarget(null)}
        onConfirm={() => {}}
      />
    </>
  );
}
