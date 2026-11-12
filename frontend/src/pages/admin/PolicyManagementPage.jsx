import { useState } from 'react';
import PageHeader from '../../components/common/PageHeader.jsx';
import ExamPeriodManager from '../../components/admin/ExamPeriodManager.jsx';
import PolicyParameterForm from '../../components/admin/PolicyParameterForm.jsx';

/**
 * 예약 정책 관리: 시험 기간(US-07) · 정책 파라미터(US-07 AC5, US-11 AC4)
 *
 * 데이터 흐름(예정)
 *  - 시험 기간: adminApi.getExamPeriods / createExamPeriod / deleteExamPeriod
 *  - 파라미터: adminApi.updatePolicy(policyKey, { policyValue })
 *
 * TODO(US-07): 정책 파라미터 목록 조회 엔드포인트가 설계서에 없다. 백엔드와 응답 출처를 확정한다.
 */
export default function PolicyManagementPage() {
  const [examPeriods] = useState([]);
  const [policies] = useState([]);

  return (
    <>
      <PageHeader title="정책 관리" stories={['US-07', 'US-11']} />
      <ExamPeriodManager examPeriods={examPeriods} onCreate={() => {}} onDelete={() => {}} />
      <PolicyParameterForm policies={policies} onSave={() => {}} />
    </>
  );
}
