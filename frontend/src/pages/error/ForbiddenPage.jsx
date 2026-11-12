import { Link } from 'react-router';
import { paths } from '../../router/paths.js';

/** 403 권한 없음 (US-02 AC4) */
export default function ForbiddenPage() {
  return (
    <div className="error-page">
      <h2>접근 권한이 없습니다</h2>
      <Link to={paths.root}>처음으로</Link>
    </div>
  );
}
