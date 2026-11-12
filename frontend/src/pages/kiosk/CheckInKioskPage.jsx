import PageHeader from '../../components/common/PageHeader.jsx';
import Placeholder from '../../components/common/Placeholder.jsx';
import QrScanner from '../../components/qr/QrScanner.jsx';

/**
 * 도서관 내 체크인 단말기 (US-09, Sprint 2)
 * 사용자 로그인 없이 단말기 키(deviceClient)로 인증한다.
 *
 * 데이터 흐름(예정): QrScanner.onScan(qrToken) → checkInApi.checkIn({ qrToken }) → 결과 표시
 */
export default function CheckInKioskPage() {
  // TODO(US-09): 위 데이터 흐름 구현
  return (
    <>
      <PageHeader title="QR 체크인" stories={['US-09']} />
      <QrScanner onScan={() => {}} />
      <Placeholder
        name="CheckInResult"
        story="US-09"
        todos={[
          '성공: 체크인 완료 메시지 (AC2)',
          '시작 전: 체크인 가능 시각 안내 / 15분 초과: 거부 (AC3, TOO_EARLY·CHECKIN_EXPIRED)',
          '이미 처리된 예약, 위·변조·존재하지 않는 QR: 거부 안내 (AC4)',
        ]}
      />
    </>
  );
}
