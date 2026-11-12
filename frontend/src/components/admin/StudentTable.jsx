import Placeholder from '../common/Placeholder.jsx';

/**
 * 학생 목록 (US-13 AC1). 행마다 이용 제한 설정·해제, 노쇼 초기화, 예약 강제 취소 진입점을 둔다.
 *
 * @param {{
 *   students: Array<object>,
 *   onSetRestriction: (student) => void,
 *   onReleaseRestriction: (student) => void,
 *   onResetNoShow: (student) => void,
 *   onForceCancel: (student) => void,
 * }} props
 */
export default function StudentTable({ students }) {
  return (
    <Placeholder
      name="StudentTable"
      story="US-13"
      todos={[
        '학번, 계정 상태, 이용 제한 여부, 노쇼 횟수 표시 (AC1)',
        `결과 ${students?.length ?? 0}건, 결과가 없으면 안내 (AC1)`,
        '행 동작: 이용 제한 설정·해제, 노쇼 초기화, 예약 강제 취소(US-14)',
      ]}
    />
  );
}
