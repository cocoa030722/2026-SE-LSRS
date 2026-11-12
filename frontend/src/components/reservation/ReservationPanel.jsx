import Placeholder from '../common/Placeholder.jsx';

/**
 * 선택한 좌석·시간대 확인 및 예약 요청 (US-06).
 *
 * @param {{
 *   seat: object | null,
 *   timeSlot: { date: string, start: string, end: string } | null,
 *   onReserve: () => void,
 * }} props
 */
export default function ReservationPanel({ seat, timeSlot, onReserve }) {
  return (
    <Placeholder
      name="ReservationPanel"
      story="US-06"
      todos={[
        '선택 좌석·시간대 요약 및 예약 버튼',
        '서버 거부 사유 안내: 본인 중복, 좌석 충돌, 정책 위반, 이용 제한(해제일) (AC2·3·4)',
      ]}
    />
  );
}
