import { useState } from 'react';
import { useNavigate } from 'react-router';
import PageHeader from '../../components/common/PageHeader.jsx';
import ErrorMessage from '../../components/common/ErrorMessage.jsx';
import { userApi } from '../../api/userApi.js';
import { useAuth } from '../../auth/useAuth.js';
import { Role } from '../../constants/roles.js';
import { paths } from '../../router/paths.js';
import { parseApiError } from '../../utils/apiError.js';

/** 내 계정: 비밀번호 변경(US-03, Sprint 2) · 회원 탈퇴(US-04, 학생) */
export default function AccountPage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  /** US-04 AC1·2: 탈퇴 성공 시 로그아웃, 진행 중 예약·이용 제한이면 서버 안내를 보여 주고 계정은 유지된다 */
  const withdraw = async () => {
    if (!window.confirm('정말 탈퇴할까요? 개인정보가 비식별화되며 되돌릴 수 없습니다.')) return;
    setSubmitting(true);
    setError(null);
    try {
      await userApi.withdraw();
      await logout();
      navigate(paths.login, { replace: true });
    } catch (e) {
      setError(parseApiError(e));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <>
      <PageHeader title="내 계정" stories={['US-03', 'US-04']} />
      <section className="panel">
        <h3>비밀번호 변경</h3>
        <p className="hint">비밀번호 변경·재설정(US-03)은 Sprint 2에서 제공합니다.</p>
      </section>
      {user?.role === Role.STUDENT && (
        <section className="panel danger-zone">
          <h3>회원 탈퇴</h3>
          <p className="hint">진행 중인 예약이 있거나 이용 제한(패널티) 중이면 탈퇴할 수 없습니다.</p>
          <button type="button" className="danger" disabled={submitting} onClick={withdraw}>
            {submitting ? '처리 중…' : '회원 탈퇴'}
          </button>
          <ErrorMessage error={error} />
        </section>
      )}
    </>
  );
}
