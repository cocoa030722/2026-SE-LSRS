import { useCallback, useEffect, useState } from 'react';
import PageHeader from '../../components/common/PageHeader.jsx';
import ErrorMessage from '../../components/common/ErrorMessage.jsx';
import PolicyNotice from '../../components/seat/PolicyNotice.jsx';
import SeatGrid from '../../components/seat/SeatGrid.jsx';
import SeatLegend from '../../components/seat/SeatLegend.jsx';
import TimeSlotSelector from '../../components/reservation/TimeSlotSelector.jsx';
import ReservationPanel from '../../components/reservation/ReservationPanel.jsx';
import { policyApi } from '../../api/policyApi.js';
import { reservationApi } from '../../api/reservationApi.js';
import { seatApi } from '../../api/seatApi.js';
import { useAutoRefresh } from '../../hooks/useAutoRefresh.js';
import { parseApiError } from '../../utils/apiError.js';
import { defaultSlot, normalizeSlot, toLocalDateTime } from '../../utils/timeSlot.js';

/** 좌석 현황 자동 갱신 주기 (US-05 AC3) */
const REFRESH_MS = 30_000;

/**
 * 좌석 현황 조회 및 예약 (US-05·06, US-07 AC3)
 *
 * 데이터 흐름
 *  1. [US-07] 선택 날짜(없으면 오늘)의 적용 정책: policyApi.getCurrent(date) → policy (운영 시간 포함)
 *  2. [US-05] timeSlot 변경 시·30초마다 seatApi.getSeats({ date, start, end }) → seats (AC1·3)
 *     예약 가능 기간 밖이면 서버 안내 메시지를 표시한다 (AC4)
 *  3. [US-06] 예약 → reservationApi.create({ seatId, startAt, endAt }) → 결과 표시 후 좌석 다시 조회 (AC1)
 */
export default function SeatMapPage() {
  const [timeSlot, setTimeSlot] = useState(null);
  const [policy, setPolicy] = useState(null);
  const [policyError, setPolicyError] = useState(null);

  const [seats, setSeats] = useState([]);
  const [seatError, setSeatError] = useState(null);
  const [selectedSeatId, setSelectedSeatId] = useState(null);

  const [submitting, setSubmitting] = useState(false);
  const [reserveError, setReserveError] = useState(null);
  const [result, setResult] = useState(null);

  const targetDate = timeSlot?.date;

  useEffect(() => {
    let ignore = false;
    policyApi
      .getCurrent(targetDate)
      .then((response) => {
        if (ignore) return;
        setPolicy(response.data);
        setPolicyError(null);
        // 첫 진입 시 기본 시간대, 날짜가 바뀌어 정책(최대 이용 시간)이 달라지면 시간대를 보정
        setTimeSlot((prev) => (prev ? normalizeSlot(prev, response.data) : defaultSlot(response.data)));
      })
      .catch((error) => {
        if (!ignore) setPolicyError(parseApiError(error));
      });
    return () => {
      ignore = true;
    };
  }, [targetDate]);

  const loadSeats = useCallback(async () => {
    if (!timeSlot) return;
    try {
      const response = await seatApi.getSeats(timeSlot);
      setSeats(response.data);
      setSeatError(null);
    } catch (error) {
      setSeats([]);
      setSeatError(parseApiError(error));
    }
  }, [timeSlot]);

  useEffect(() => {
    loadSeats();
  }, [loadSeats]);

  useAutoRefresh(loadSeats, timeSlot ? REFRESH_MS : null);

  const changeTimeSlot = (next) => {
    setTimeSlot(next);
    setResult(null);
    setReserveError(null);
  };

  const selectSeat = (seat) => {
    setSelectedSeatId(seat.seatId);
    setResult(null);
    setReserveError(null);
  };

  const reserve = async () => {
    setSubmitting(true);
    setReserveError(null);
    try {
      const response = await reservationApi.create({
        seatId: selectedSeatId,
        startAt: toLocalDateTime(timeSlot.date, timeSlot.start),
        endAt: toLocalDateTime(timeSlot.date, timeSlot.end),
      });
      setResult(response.data);
      setSelectedSeatId(null);
    } catch (error) {
      setReserveError(parseApiError(error));
    } finally {
      setSubmitting(false);
      // 성공·좌석 충돌 모두 최신 상태로 다시 그린다
      loadSeats();
    }
  };

  // 선택 좌석은 id 로 기억하고 상태는 최신 조회 결과에서 꺼낸다 (자동 갱신 반영)
  const selectedSeat = seats.find((seat) => seat.seatId === selectedSeatId) ?? null;

  return (
    <>
      <PageHeader title="좌석 예약" stories={['US-05', 'US-06', 'US-07']} />
      <PolicyNotice policy={policy} error={policyError} />
      <TimeSlotSelector value={timeSlot} onChange={changeTimeSlot} policy={policy} />
      <SeatLegend />
      {seatError ? (
        <ErrorMessage error={seatError} />
      ) : (
        <SeatGrid seats={seats} selectedSeatId={selectedSeatId} onSelect={selectSeat} />
      )}
      <ReservationPanel
        seat={selectedSeat}
        timeSlot={timeSlot}
        submitting={submitting}
        error={reserveError}
        result={result}
        onReserve={reserve}
      />
    </>
  );
}
