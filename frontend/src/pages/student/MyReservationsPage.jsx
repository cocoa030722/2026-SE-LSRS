import { useCallback, useEffect, useState } from 'react';
import PageHeader from '../../components/common/PageHeader.jsx';
import ErrorMessage from '../../components/common/ErrorMessage.jsx';
import ReservationStatusFilter from '../../components/reservation/ReservationStatusFilter.jsx';
import ReservationList from '../../components/reservation/ReservationList.jsx';
import CancelReasonDialog from '../../components/reservation/CancelReasonDialog.jsx';
import { reservationApi } from '../../api/reservationApi.js';
import { parseApiError } from '../../utils/apiError.js';

/**
 * 내 예약 조회 및 직접 취소 (US-08)
 *  1. status 변경 시 reservationApi.getMine({ status }) (AC1·2)
 *  2. 취소: 사유 선택 → reservationApi.cancel(id, { reason }) → 목록 다시 조회 (AC3·4)
 *  3. 서버 거부(이미 종결된 예약·타인 예약)는 대화상자에 사유를 표시 (AC5)
 */
export default function MyReservationsPage() {
  const [status, setStatus] = useState('');
  const [reservations, setReservations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState(null);
  const [notice, setNotice] = useState(null);

  const [cancelTarget, setCancelTarget] = useState(null);
  const [cancelling, setCancelling] = useState(false);
  const [cancelError, setCancelError] = useState(null);

  const load = useCallback(async (statusFilter) => {
    setLoading(true);
    setLoadError(null);
    try {
      const response = await reservationApi.getMine(statusFilter ? { status: statusFilter } : undefined);
      setReservations(response.data);
    } catch (error) {
      setReservations([]);
      setLoadError(parseApiError(error));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load(status);
  }, [load, status]);

  const openCancel = (reservation) => {
    setNotice(null);
    setCancelError(null);
    setCancelTarget(reservation);
  };

  const closeCancel = () => setCancelTarget(null);

  const confirmCancel = async (reason) => {
    setCancelling(true);
    setCancelError(null);
    try {
      await reservationApi.cancel(cancelTarget.reservationId, { reason });
      setCancelTarget(null);
      setNotice('예약이 취소되었습니다. 좌석은 바로 다른 학생이 예약할 수 있습니다.');
      await load(status);
    } catch (error) {
      setCancelError(parseApiError(error));
    } finally {
      setCancelling(false);
    }
  };

  return (
    <>
      <PageHeader title="내 예약" stories={['US-08']}>
        <ReservationStatusFilter value={status} onChange={setStatus} />
      </PageHeader>

      {notice && <p className="notice">{notice}</p>}
      <ErrorMessage error={loadError} />

      {loading ? (
        <p className="loading">불러오는 중…</p>
      ) : (
        !loadError && (
          <ReservationList
            reservations={reservations}
            emptyMessage={status ? '해당 상태의 예약이 없습니다.' : '예약 내역이 없습니다.'}
            onCancel={openCancel}
          />
        )
      )}

      {cancelTarget && (
        <CancelReasonDialog
          reservation={cancelTarget}
          submitting={cancelling}
          error={cancelError}
          onClose={closeCancel}
          onConfirm={confirmCancel}
        />
      )}
    </>
  );
}
