import Placeholder from '../common/Placeholder.jsx';
import { ADMIN_CANCEL_REASONS } from '../../constants/cancelReasons.js';

/**
 * 관리자 강제 취소 (FR-X-20~22, US-14).
 *
 * @param {{
 *   open: boolean,
 *   reservation: object | null,
 *   onClose: () => void,
 *   onConfirm: (body: { reason: string, applyPenalty: boolean }) => void,
 * }} props
 */
export default function ForceCancelDialog({ open }) {
  if (!open) return null;
  return (
    <Placeholder
      name="ForceCancelDialog"
      story="US-14"
      todos={[
        `사유 필수 선택: ${ADMIN_CANCEL_REASONS.map((r) => r.label).join(', ')} (AC2)`,
        '노쇼 패널티 부여 여부 선택 (AC3)',
      ]}
    />
  );
}
