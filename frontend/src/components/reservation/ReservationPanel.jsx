import { Link } from 'react-router';
import ErrorMessage from '../common/ErrorMessage.jsx';
import { SeatStatus } from '../../constants/seatStatus.js';
import { paths } from '../../router/paths.js';
import { formatTimeRange } from '../../utils/format.js';

/** 선택은 되지만 예약할 수 없는 좌석의 안내 (US-05 AC2) */
const UNAVAILABLE_MESSAGE = {
  [SeatStatus.IN_USE]: '사용 중인 좌석입니다. 다른 좌석을 선택해 주세요.',
  [SeatStatus.RESERVED]: '이미 예약된 좌석입니다. 다른 좌석을 선택해 주세요.',
  [SeatStatus.MINE]: '이 시간대에 이미 예약한 좌석입니다. 내 예약에서 확인할 수 있습니다.',
};

/**
 * 선택한 좌석·시간대 확인 및 예약 요청 (US-06).
 *
 * @param {{
 *   seat: { seatNo: string, status: string } | null,
 *   timeSlot: { date: string, start: string, end: string } | null,
 *   submitting: boolean,
 *   error: { message: string } | null,   서버 거부 사유: 본인 중복, 좌석 충돌, 정책 위반, 이용 제한(해제일) (AC2·3·4)
 *   result: { reservationId, seatNo, startAt, endAt } | null,   생성된 예약 (AC1)
 *   onReserve: () => void,
 * }} props
 */
export default function ReservationPanel({ seat, timeSlot, submitting, error, result, onReserve }) {
  if (result) {
    return (
      <section className="panel reservation-panel">
        <p className="notice">
          예약이 완료되었습니다. {result.seatNo} · {formatTimeRange(result.startAt, result.endAt)}
        </p>
        <p className="hint">
          시작 시각부터 체크인 가능 시간 안에 QR로 체크인해 주세요. <Link to={paths.myReservations}>내 예약 보기</Link>
        </p>
      </section>
    );
  }

  if (!seat || !timeSlot) {
    return (
      <section className="panel reservation-panel">
        <p className="hint">날짜·시간대를 고르고 좌석 배치도에서 좌석을 선택해 주세요.</p>
      </section>
    );
  }

  const unavailable = UNAVAILABLE_MESSAGE[seat.status];

  return (
    <section className="panel reservation-panel">
      <h3>
        {seat.seatNo} · {timeSlot.date} {timeSlot.start} ~ {timeSlot.end}
      </h3>
      {unavailable ? (
        <p className="hint">{unavailable}</p>
      ) : (
        <button type="button" className="primary" disabled={submitting} onClick={onReserve}>
          {submitting ? '예약 중…' : '예약하기'}
        </button>
      )}
      <ErrorMessage error={error} />
    </section>
  );
}
