import Placeholder from '../common/Placeholder.jsx';

/**
 * 단말기 카메라 QR 스캐너 (FR-C-02, US-09). html5-qrcode 로 구현 예정.
 * @param {{ onScan: (qrToken: string) => void }} props
 */
export default function QrScanner({ onScan }) {
  return (
    <Placeholder
      name="QrScanner"
      story="US-09"
      todos={['html5-qrcode 카메라 스캔 시작·정지, 인식 결과를 onScan 으로 전달']}
    />
  );
}
