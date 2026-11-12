import Placeholder from '../common/Placeholder.jsx';

/**
 * 예약 QR 코드 표시 (FR-C-01, US-09 AC1). 토큰은 서버가 서명해 내려준다.
 * @param {{ qrToken: string | null }} props
 */
export default function ReservationQrCode() {
  return (
    <Placeholder
      name="ReservationQrCode"
      story="US-09"
      todos={['qrcode.react 의 QRCodeSVG 로 예약 상세 응답의 QR 토큰 표시']}
    />
  );
}
