import { useParams } from 'react-router';
import PageHeader from '../../components/common/PageHeader.jsx';
import Placeholder from '../../components/common/Placeholder.jsx';
import ReservationQrCode from '../../components/qr/ReservationQrCode.jsx';

/**
 * 예약 완료·상세 + QR (US-06 AC1, US-09 AC1)
 * 예약 생성 직후 확인 화면과 내 예약 상세를 같은 화면으로 쓴다.
 *
 * 데이터 흐름(예정): reservationApi.getById(reservationId) → reservation (본인만, US-02 AC5)
 */
export default function ReservationDetailPage() {
  const { reservationId } = useParams();
  // TODO(US-06·09): 예약 상세 조회. QR 토큰은 응답에서 꺼낸다(Sprint 2).
  const reservation = null;

  return (
    <>
      <PageHeader title={`예약 상세 #${reservationId}`} stories={['US-06', 'US-09']} />
      <Placeholder
        name="ReservationSummary"
        story="US-06"
        todos={['좌석 번호, 날짜·시간대, 상태 표시 (US-06 AC1)']}
      />
      <ReservationQrCode qrToken={reservation?.qrToken ?? null} />
    </>
  );
}
