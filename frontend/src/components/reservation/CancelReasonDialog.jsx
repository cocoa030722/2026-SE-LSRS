import { useState } from 'react';
import ErrorMessage from '../common/ErrorMessage.jsx';
import { STUDENT_CANCEL_REASONS } from '../../constants/cancelReasons.js';
import { formatTimeRange } from '../../utils/format.js';

/**
 * 학생 직접 취소 사유 선택 (FR-X-02, US-08 AC3).
 * 열 때마다 새로 마운트되도록 부모에서 open 일 때만 렌더링한다.
 *
 * @param {{
 *   reservation: { seatNo, startAt, endAt },
 *   submitting: boolean,
 *   error: { message: string } | null,
 *   onClose: () => void,
 *   onConfirm: (reasonCode: string) => void,
 * }} props
 */
export default function CancelReasonDialog({ reservation, submitting, error, onClose, onConfirm }) {
  const [reason, setReason] = useState('');

  const handleSubmit = (event) => {
    event.preventDefault();
    if (reason) onConfirm(reason);
  };

  return (
    <div className="dialog-backdrop">
      <form className="dialog" role="dialog" aria-labelledby="cancel-dialog-title" onSubmit={handleSubmit}>
        <h3 id="cancel-dialog-title">예약 취소</h3>
        <p className="dialog-summary">
          {reservation.seatNo} · {formatTimeRange(reservation.startAt, reservation.endAt)}
        </p>
        <fieldset className="reason-options">
          <legend>취소 사유</legend>
          {STUDENT_CANCEL_REASONS.map((option) => (
            <label key={option.code}>
              <input
                type="radio"
                name="cancel-reason"
                value={option.code}
                checked={reason === option.code}
                onChange={() => setReason(option.code)}
              />
              {option.label}
            </label>
          ))}
        </fieldset>
        <p className="hint">직접 취소는 노쇼로 기록되지 않습니다.</p>
        <ErrorMessage error={error} />
        <div className="dialog-actions">
          <button type="button" onClick={onClose} disabled={submitting}>
            닫기
          </button>
          <button type="submit" className="primary" disabled={!reason || submitting}>
            {submitting ? '취소 중…' : '예약 취소'}
          </button>
        </div>
      </form>
    </div>
  );
}
