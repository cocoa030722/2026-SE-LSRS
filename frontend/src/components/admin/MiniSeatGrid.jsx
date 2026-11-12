import Placeholder from '../common/Placeholder.jsx';
import { DASHBOARD_SEAT_STATUS_LABEL } from '../../constants/seatStatus.js';

/**
 * 전체 좌석 축소 그리드 (FR-A-02, US-12 AC2).
 * @param {{ seats: Array<object> }} props
 */
export default function MiniSeatGrid({ seats }) {
  return (
    <Placeholder
      name="MiniSeatGrid"
      story="US-12"
      todos={[
        `상태 구분: ${Object.values(DASHBOARD_SEAT_STATUS_LABEL).join(', ')}`,
        `좌석 ${seats?.length ?? 0}개 렌더링`,
      ]}
    />
  );
}
