import { useState } from 'react';
import PageHeader from '../../components/common/PageHeader.jsx';
import StudentSearchForm from '../../components/admin/StudentSearchForm.jsx';
import StudentTable from '../../components/admin/StudentTable.jsx';
import RestrictionDialog from '../../components/admin/RestrictionDialog.jsx';
import ForceCancelDialog from '../../components/admin/ForceCancelDialog.jsx';

/**
 * 학생 검색·이용 제한 관리 (US-13) 및 예약 강제 취소 (US-14), Sprint 2
 *
 * 데이터 흐름(예정)
 *  - 검색: adminApi.searchStudents({ studentNo | seatNo })
 *  - 제한 설정/해제: adminApi.setRestriction / releaseRestriction
 *  - 노쇼 초기화: adminApi.resetNoShowCount
 *  - 강제 취소: adminApi.forceCancelReservation(id, { reason, applyPenalty })
 *
 * TODO(US-14): 강제 취소 대상 예약을 어디서 고르는지(좌석 번호 검색 결과 등) 백엔드 응답과 함께 확정한다.
 */
export default function StudentManagementPage() {
  const [students] = useState([]);
  const [restrictionTarget, setRestrictionTarget] = useState(null);
  const [cancelTarget, setCancelTarget] = useState(null);

  return (
    <>
      <PageHeader title="학생 관리" stories={['US-13', 'US-14']} />
      <StudentSearchForm onSearch={() => {}} />
      <StudentTable
        students={students}
        onSetRestriction={setRestrictionTarget}
        onReleaseRestriction={() => {}}
        onResetNoShow={() => {}}
        onForceCancel={setCancelTarget}
      />
      <RestrictionDialog
        open={restrictionTarget !== null}
        student={restrictionTarget}
        onClose={() => setRestrictionTarget(null)}
        onConfirm={() => {}}
      />
      <ForceCancelDialog
        open={cancelTarget !== null}
        reservation={cancelTarget}
        onClose={() => setCancelTarget(null)}
        onConfirm={() => {}}
      />
    </>
  );
}
