import { Link } from 'react-router';
import { paths } from '../../router/paths.js';

export default function NotFoundPage() {
  return (
    <div className="error-page">
      <h2>페이지를 찾을 수 없습니다</h2>
      <Link to={paths.root}>처음으로</Link>
    </div>
  );
}
