import { useState } from 'react';
import PageHeader from '../../components/common/PageHeader.jsx';
import PolicyNotice from '../../components/seat/PolicyNotice.jsx';
import SeatGrid from '../../components/seat/SeatGrid.jsx';
import SeatLegend from '../../components/seat/SeatLegend.jsx';
import TimeSlotSelector from '../../components/reservation/TimeSlotSelector.jsx';
import ReservationPanel from '../../components/reservation/ReservationPanel.jsx';

/**
 * 좌석 현황 조회 및 예약 (US-05·06, US-07 AC3)
 *
 * 데이터 흐름(예정)
 *  1. policyApi.getCurrent()          → policy
 *  2. timeSlot 변경 시 seatApi.getSeats({ date, start, end }) → seats
 *  3. 예약 → reservationApi.create() → 성공 시 예약 상세(QR) 화면으로 이동 (US-06 AC1, US-09 AC1)
 */
export default function SeatMapPage() {
  // TODO(US-05·06·07): 위 데이터 흐름 구현. 자동 갱신은 hooks/useAutoRefresh 사용 (US-05 AC3)
  const [policy] = useState(null);
  const [seats] = useState([]);
  const [timeSlot, setTimeSlot] = useState(null);
  const [selectedSeat, setSelectedSeat] = useState(null);

  return (
    <>
      <PageHeader title="좌석 예약" stories={['US-05', 'US-06', 'US-07']} />
      <PolicyNotice policy={policy} />
      <TimeSlotSelector value={timeSlot} onChange={setTimeSlot} policy={policy} />
      <SeatLegend />
      <SeatGrid seats={seats} selectedSeatId={selectedSeat?.seatId} onSelect={setSelectedSeat} />
      <ReservationPanel seat={selectedSeat} timeSlot={timeSlot} onReserve={() => {}} />
    </>
  );
}
