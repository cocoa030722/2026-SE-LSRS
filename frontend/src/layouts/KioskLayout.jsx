import { Outlet } from 'react-router';

/** 도서관 내 체크인 단말기 전용 전체 화면 레이아웃 (US-09, FR-C-02) */
export default function KioskLayout() {
  return (
    <div className="kiosk-layout">
      <Outlet />
    </div>
  );
}
