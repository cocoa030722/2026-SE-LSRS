import Placeholder from '../common/Placeholder.jsx';

/**
 * 정책 파라미터 수정 (NFR 4.1, US-07 AC5, US-11 AC4).
 * 키 목록은 서버가 내려주는 값을 그대로 렌더링한다.
 *
 * @param {{
 *   policies: Array<{ policyKey: string, policyValue: string }>,
 *   onSave: (policyKey: string, policyValue: string) => void,
 * }} props
 */
export default function PolicyParameterForm({ policies }) {
  return (
    <Placeholder
      name="PolicyParameterForm"
      story="US-07"
      todos={[`정책 파라미터 ${policies?.length ?? 0}건 조회·수정`]}
    />
  );
}
