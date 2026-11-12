import Placeholder from '../common/Placeholder.jsx';

/**
 * 관리자 권한 부여·회수 (FR-A-13, US-16).
 * @param {{ onSubmit: (userId: number, role: string) => void }} props
 */
export default function UserRoleForm() {
  return (
    <Placeholder
      name="UserRoleForm"
      story="US-16"
      todos={[
        '대상 사용자 선택 → ADMIN / STUDENT 역할 변경 (AC1·2)',
        '마지막 관리자 권한 회수 시 거부 안내 (AC3)',
      ]}
    />
  );
}
