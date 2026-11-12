import PageHeader from '../../components/common/PageHeader.jsx';
import Placeholder from '../../components/common/Placeholder.jsx';

/** 이메일 인증 (FR-U-02~04, US-01) */
export default function VerifyEmailPage() {
  // TODO(US-01 AC2): authApi.verifyEmail → 성공 시 로그인 화면으로 이동
  return (
    <>
      <PageHeader title="이메일 인증" stories={['US-01']} />
      <Placeholder
        name="VerifyEmailForm"
        story="US-01"
        todos={[
          '6자리 인증 코드 입력 (AC2)',
          '잘못되었거나 이미 사용된 코드는 오류 안내 (AC5)',
        ]}
      />
    </>
  );
}
