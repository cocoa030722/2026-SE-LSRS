/**
 * 서버 오류 { code, message } 또는 입력 검증 오류를 표시한다.
 * @param {{ error: { code?: string | null, message: string } | null }} props
 */
export default function ErrorMessage({ error }) {
  if (!error) return null;
  return (
    <p className="error-message" role="alert">
      {error.message}
    </p>
  );
}
