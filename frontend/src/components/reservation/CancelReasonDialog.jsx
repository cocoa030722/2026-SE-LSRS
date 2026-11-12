import Placeholder from '../common/Placeholder.jsx';
import { STUDENT_CANCEL_REASONS } from '../../constants/cancelReasons.js';

/**
 * 학생 직접 취소 사유 선택 (FR-X-02, US-08 AC3).
 *
 * @param {{
 *   open: boolean,
 *   onClose: () => void,
 *   onConfirm: (reasonCode: string) => void,
 * }} props
 */
export default function CancelReasonDialog({ open, onClose, onConfirm }) {
  if (!open) return null;
  return (
    <Placeholder
      name="CancelReasonDialog"
      story="US-08"
      todos={[`사유 선택: ${STUDENT_CANCEL_REASONS.map((r) => r.label).join(', ')}`]}
    />
  );
}
