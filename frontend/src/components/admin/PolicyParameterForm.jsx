import { useState } from 'react';
import ErrorMessage from '../common/ErrorMessage.jsx';
import EmptyState from '../common/EmptyState.jsx';
import { POLICY_KEY_LABEL } from '../../constants/policyKeys.js';

/**
 * 정책 파라미터 수정 (NFR 4.1, US-07 AC5, US-11 AC4).
 * 키 목록은 서버가 내려주는 값을 그대로 렌더링한다. 변경은 이후 생성되는 예약부터 적용된다.
 *
 * @param {{
 *   policies: Array<{ policyKey: string, policyValue: string, updatedAt: string }>,
 *   savingKey: string | null,
 *   error: { message: string } | null,
 *   onSave: (policyKey: string, policyValue: string) => Promise<boolean>,
 * }} props onSave 는 성공 여부를 돌려준다 (실패 시 입력값 유지)
 */
export default function PolicyParameterForm({ policies, savingKey, error, onSave }) {
  const [drafts, setDrafts] = useState({});

  const valueOf = (policy) => drafts[policy.policyKey] ?? policy.policyValue;

  const handleChange = (policyKey, value) => setDrafts((prev) => ({ ...prev, [policyKey]: value }));

  const handleSave = async (policy) => {
    const saved = await onSave(policy.policyKey, valueOf(policy));
    if (!saved) return;
    setDrafts((prev) => {
      const next = { ...prev };
      delete next[policy.policyKey];
      return next;
    });
  };

  return (
    <section className="panel">
      <h3>정책 파라미터</h3>
      <p className="hint">값을 바꾸면 이후 생성되는 예약부터 적용되고, 이미 생성된 예약은 바뀌지 않습니다.</p>
      {policies.length === 0 ? (
        <EmptyState message="정책 파라미터가 없습니다." />
      ) : (
        <table className="data-table">
          <thead>
            <tr>
              <th>항목</th>
              <th>값</th>
              <th>변경 시각</th>
              <th aria-label="동작" />
            </tr>
          </thead>
          <tbody>
            {policies.map((policy) => {
              const value = valueOf(policy);
              const changed = value !== policy.policyValue;
              return (
                <tr key={policy.policyKey}>
                  <td>
                    {POLICY_KEY_LABEL[policy.policyKey] ?? policy.policyKey}
                    <div className="hint">{policy.policyKey}</div>
                  </td>
                  <td>
                    <input
                      className="policy-value-input"
                      value={value}
                      onChange={(e) => handleChange(policy.policyKey, e.target.value)}
                      aria-label={`${policy.policyKey} 값`}
                    />
                  </td>
                  <td>{policy.updatedAt?.replace('T', ' ').slice(0, 16)}</td>
                  <td>
                    <button
                      type="button"
                      onClick={() => handleSave(policy)}
                      disabled={!changed || savingKey === policy.policyKey}
                    >
                      저장
                    </button>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      )}
      <ErrorMessage error={error} />
    </section>
  );
}
