import { useEffect, useState } from 'react';
import PageHeader from '../../components/common/PageHeader.jsx';
import PolicyNotice from '../../components/seat/PolicyNotice.jsx';
import SeatGrid from '../../components/seat/SeatGrid.jsx';
import SeatLegend from '../../components/seat/SeatLegend.jsx';
import TimeSlotSelector from '../../components/reservation/TimeSlotSelector.jsx';
import ReservationPanel from '../../components/reservation/ReservationPanel.jsx';
import { policyApi } from '../../api/policyApi.js';
import { parseApiError } from '../../utils/apiError.js';

/**
 * 좌석 현황 조회 및 예약 (US-05·06, US-07 AC3)
 *
 * 데이터 흐름
 *  1. [US-07] 선택 날짜(없으면 오늘)의 적용 정책: policyApi.getCurrent(date) → policy
 *  2. [US-05, 예정] timeSlot 변경 시 seatApi.getSeats({ date, start, end }) → seats
 *  3. [US-06, 예정] 예약 → reservationApi.create() → 성공 시 예약 상세(QR) 화면으로 이동 (US-06 AC1, US-09 AC1)
 */
export default function SeatMapPage() {
  // TODO(US-05·06): 위 2·3 데이터 흐름 구현. 자동 갱신은 hooks/useAutoRefresh 사용 (US-05 AC3)
  const [seats] = useState([]);
  const [timeSlot, setTimeSlot] = useState(null);
  const [selectedSeat, setSelectedSeat] = useState(null);

  const [policy, setPolicy] = useState(null);
  const [policyError, setPolicyError] = useState(null);
  const targetDate = timeSlot?.date;

  useEffect(() => {
    let ignore = false;
    policyApi
      .getCurrent(targetDate)
      .then((response) => {
        if (ignore) return;
        setPolicy(response.data);
        setPolicyError(null);
      })
      .catch((error) => {
        if (!ignore) setPolicyError(parseApiError(error));
      });
    return () => {
      ignore = true;
    };
  }, [targetDate]);

  return (
    <>
      <PageHeader title="좌석 예약" stories={['US-05', 'US-06', 'US-07']} />
      <PolicyNotice policy={policy} error={policyError} />
      <TimeSlotSelector value={timeSlot} onChange={setTimeSlot} policy={policy} />
      <SeatLegend />
      <SeatGrid seats={seats} selectedSeatId={selectedSeat?.seatId} onSelect={setSelectedSeat} />
      <ReservationPanel seat={selectedSeat} timeSlot={timeSlot} onReserve={() => {}} />
    </>
  );
}
