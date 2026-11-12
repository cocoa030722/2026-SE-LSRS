import { useState } from 'react';
import EmptyState from '../common/EmptyState.jsx';
import ErrorMessage from '../common/ErrorMessage.jsx';

/**
 * 시험 기간 등록·조회·삭제 (FR-A-20, US-07 AC1·4).
 * 기간 검증(시작일 > 종료일, 기존 기간과 겹침)은 서버가 하고, 화면은 서버 안내를 그대로 보여 준다.
 *
 * @param {{
 *   examPeriods: Array<{ examPeriodId: number, startDate: string, endDate: string }>,
 *   busy: boolean,
 *   error: { message: string } | null,
 *   onCreate: (body: { startDate: string, endDate: string }) => Promise<boolean>,
 *   onDelete: (examPeriodId: number) => void,
 * }} props onCreate 는 성공 여부를 돌려준다 (성공 시 입력칸 초기화)
 */
export default function ExamPeriodManager({ examPeriods, busy, error, onCreate, onDelete }) {
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');

  const handleSubmit = async (event) => {
    event.preventDefault();
    const created = await onCreate({ startDate, endDate });
    if (created) {
      setStartDate('');
      setEndDate('');
    }
  };

  return (
    <section className="panel">
      <h3>시험 기간</h3>
      <p className="hint">
        오늘 또는 예약 대상일이 시험 기간이면 시험 기간 정책(EXAM_DAYS·EXAM_MAX_HOURS)이, 그 외에는 평상시
        정책(NORMAL_DAYS·NORMAL_MAX_HOURS)이 적용됩니다. 기간 시작·종료일은 포함됩니다.
      </p>

      {examPeriods.length === 0 ? (
        <EmptyState message="등록된 시험 기간이 없습니다." />
      ) : (
        <table className="data-table">
          <thead>
            <tr>
              <th>시작일</th>
              <th>종료일</th>
              <th aria-label="동작" />
            </tr>
          </thead>
          <tbody>
            {examPeriods.map((period) => (
              <tr key={period.examPeriodId}>
                <td>{period.startDate}</td>
                <td>{period.endDate}</td>
                <td>
                  <button type="button" onClick={() => onDelete(period.examPeriodId)} disabled={busy}>
                    삭제
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      <form className="inline-form" onSubmit={handleSubmit}>
        <label>
          시작일
          <input type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} required />
        </label>
        <label>
          종료일
          <input type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} required />
        </label>
        <button type="submit" className="primary" disabled={busy}>
          등록
        </button>
      </form>
      <ErrorMessage error={error} />
    </section>
  );
}
