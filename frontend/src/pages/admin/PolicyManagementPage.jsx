import { useCallback, useEffect, useState } from 'react';
import PageHeader from '../../components/common/PageHeader.jsx';
import ErrorMessage from '../../components/common/ErrorMessage.jsx';
import ExamPeriodManager from '../../components/admin/ExamPeriodManager.jsx';
import PolicyParameterForm from '../../components/admin/PolicyParameterForm.jsx';
import { adminApi } from '../../api/adminApi.js';
import { parseApiError } from '../../utils/apiError.js';

/**
 * 예약 정책 관리: 시험 기간(US-07 AC1·4) · 정책 파라미터(US-07 AC5, US-11 AC4)
 *  - 시험 기간: adminApi.getExamPeriods / createExamPeriod / deleteExamPeriod
 *  - 파라미터: adminApi.getPolicies / updatePolicy(policyKey, { policyValue })
 */
export default function PolicyManagementPage() {
  const [examPeriods, setExamPeriods] = useState([]);
  const [policies, setPolicies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState(null);
  const [notice, setNotice] = useState(null);

  const [examBusy, setExamBusy] = useState(false);
  const [examError, setExamError] = useState(null);
  const [savingKey, setSavingKey] = useState(null);
  const [policyError, setPolicyError] = useState(null);

  const loadExamPeriods = useCallback(async () => {
    const response = await adminApi.getExamPeriods();
    setExamPeriods(response.data);
  }, []);

  const loadPolicies = useCallback(async () => {
    const response = await adminApi.getPolicies();
    setPolicies(response.data);
  }, []);

  useEffect(() => {
    Promise.all([loadExamPeriods(), loadPolicies()])
      .catch((error) => setLoadError(parseApiError(error)))
      .finally(() => setLoading(false));
  }, [loadExamPeriods, loadPolicies]);

  const createExamPeriod = async (body) => {
    setExamBusy(true);
    setExamError(null);
    setNotice(null);
    try {
      await adminApi.createExamPeriod(body);
      await loadExamPeriods();
      setNotice(`시험 기간 ${body.startDate} ~ ${body.endDate} 을(를) 등록했습니다.`);
      return true;
    } catch (error) {
      setExamError(parseApiError(error));
      return false;
    } finally {
      setExamBusy(false);
    }
  };

  const deleteExamPeriod = async (examPeriodId) => {
    if (!window.confirm('이 시험 기간을 삭제할까요?')) return;
    setExamBusy(true);
    setExamError(null);
    setNotice(null);
    try {
      await adminApi.deleteExamPeriod(examPeriodId);
      await loadExamPeriods();
      setNotice('시험 기간을 삭제했습니다.');
    } catch (error) {
      setExamError(parseApiError(error));
    } finally {
      setExamBusy(false);
    }
  };

  const savePolicy = async (policyKey, policyValue) => {
    setSavingKey(policyKey);
    setPolicyError(null);
    setNotice(null);
    try {
      await adminApi.updatePolicy(policyKey, { policyValue });
      await loadPolicies();
      setNotice(`${policyKey} 값을 ${policyValue.trim()}(으)로 변경했습니다.`);
      return true;
    } catch (error) {
      setPolicyError(parseApiError(error));
      return false;
    } finally {
      setSavingKey(null);
    }
  };

  return (
    <>
      <PageHeader title="정책 관리" stories={['US-07']} />
      {notice && <p className="notice">{notice}</p>}
      <ErrorMessage error={loadError} />
      {loading ? (
        <p className="loading">불러오는 중…</p>
      ) : (
        !loadError && (
          <>
            <ExamPeriodManager
              examPeriods={examPeriods}
              busy={examBusy}
              error={examError}
              onCreate={createExamPeriod}
              onDelete={deleteExamPeriod}
            />
            <PolicyParameterForm
              policies={policies}
              savingKey={savingKey}
              error={policyError}
              onSave={savePolicy}
            />
          </>
        )
      )}
    </>
  );
}
