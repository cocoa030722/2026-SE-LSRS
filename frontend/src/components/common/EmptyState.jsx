/**
 * 조회 결과가 없을 때의 안내 문구 (US-08 AC1, US-13 AC1, US-15 AC4 등).
 * @param {{ message: string }} props
 */
export default function EmptyState({ message }) {
  return <p className="empty-state">{message}</p>;
}
