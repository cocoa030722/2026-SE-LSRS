import Placeholder from '../common/Placeholder.jsx';

/**
 * 이용 제한 해제일 설정 (FR-A-11·21, US-13 AC2·3).
 *
 * @param {{
 *   open: boolean,
 *   student: object | null,
 *   onClose: () => void,
 *   onConfirm: (until: string) => void,
 * }} props
 */
export default function RestrictionDialog({ open }) {
  if (!open) return null;
  return (
    <Placeholder
      name="RestrictionDialog"
      story="US-13"
      todos={['해제일 입력 (과거 날짜는 거부, AC3)']}
    />
  );
}
