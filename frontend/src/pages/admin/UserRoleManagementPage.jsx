import PageHeader from '../../components/common/PageHeader.jsx';
import UserRoleForm from '../../components/admin/UserRoleForm.jsx';

/**
 * 관리자 권한 부여·회수 (US-16, Sprint 2)
 * 데이터 흐름(예정): adminApi.updateUserRole(userId, { role })
 *
 * TODO(US-16): 대상 사용자 검색 방법(학생 검색 API 재사용 여부)을 백엔드와 확정한다.
 */
export default function UserRoleManagementPage() {
  return (
    <>
      <PageHeader title="권한 관리" stories={['US-16']} />
      <UserRoleForm onSubmit={() => {}} />
    </>
  );
}
