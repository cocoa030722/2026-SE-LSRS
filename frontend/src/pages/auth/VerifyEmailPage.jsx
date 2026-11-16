import { useState } from 'react';
import { Link, useLocation, useSearchParams } from 'react-router';
import PageHeader from '../../components/common/PageHeader.jsx';
import ErrorMessage from '../../components/common/ErrorMessage.jsx';
import { authApi } from '../../api/authApi.js';
import { paths } from '../../router/paths.js';
import { parseApiError } from '../../utils/apiError.js';

/**
 * 이메일 인증 (FR-U-02~04, US-01).
 * 메일 발송 대신 서버 콘솔에 출력된 인증 토큰을 입력한다 (?token= 쿼리로 열면 미리 채워진다).
 */
export default function VerifyEmailPage() {
  const location = useLocation();
  const [searchParams] = useSearchParams();
  const [token, setToken] = useState(searchParams.get('token') ?? '');
  const [error, setError] = useState(null);
  const [done, setDone] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      const response = await authApi.verifyEmail(token.trim());
      setDone(response.data);
    } catch (e) {
      setError(parseApiError(e));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <>
      <PageHeader title="이메일 인증" stories={['US-01']} />
      {location.state?.notice && !done && <p className="notice">{location.state.notice}</p>}
      {done ? (
        <>
          <p className="notice">{done}</p>
          <Link to={paths.login}>로그인하러 가기</Link>
        </>
      ) : (
        <form className="auth-form" onSubmit={handleSubmit}>
          <label>
            인증 토큰
            <input value={token} onChange={(e) => setToken(e.target.value)} required />
          </label>
          <button type="submit" className="primary" disabled={submitting}>
            {submitting ? '확인 중…' : '인증하기'}
          </button>
          <ErrorMessage error={error} />
        </form>
      )}
    </>
  );
}
