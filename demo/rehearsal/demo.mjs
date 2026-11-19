import { chromium } from 'playwright-core';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const BASE = 'http://localhost:5173';

// bootRun 로그 파일: 인자로 받은 경로 → 없으면 저장소 루트 기준으로 찾는다 (기본: 저장소 루트의 boot.log)
const REPO_ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..', '..');
const LOG = [process.argv[2], process.argv[2] && path.join(REPO_ROOT, process.argv[2]), path.join(REPO_ROOT, 'boot.log')]
  .filter(Boolean)
  .find((p) => fs.existsSync(p));
if (!LOG) {
  console.error(`bootRun 로그 파일을 찾을 수 없습니다: ${process.argv[2] ?? '(인자 없음)'}\n저장소 루트에서 .\\gradlew.bat bootRun *> boot.log 로 실행한 뒤, node demo.mjs ..\\..\\boot.log 로 실행하세요.`);
  process.exit(1);
}
console.log('로그 파일:', LOG);
// PowerShell 5.1 의 *> 리다이렉트는 UTF-16LE(BOM FF FE)로 저장한다
const readLog = () => {
  const buf = fs.readFileSync(LOG);
  return buf[0] === 0xff && buf[1] === 0xfe ? buf.toString('utf16le') : buf.toString('utf8');
};
const PW = 'Passw0rd!';
const fmt = (d) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
const now = new Date();
// 시연 날짜는 seed-demo.sql 과 같은 절대 날짜로 고정한다 (데모일 2026-11-19)
const TODAY = '2026-11-19';
const TMR = '2026-11-20';
const D5 = '2026-11-24';
// Seed 의 A-03 체크인 시간대 (11-19 20~22시)
const HOUR = '20:00';
const HOUR2 = '22:00';

const browser = await chromium.launch({ executablePath: process.env.EDGE_PATH ?? 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe', headless: true });
let pass = 0, fail = 0;
const check = async (name, fn) => {
  try { await fn(); pass++; console.log('PASS', name); }
  catch (e) { fail++; console.log('FAIL', name, '\n     ', String(e.message).split('\n')[0]); }
};
const newPage = async () => { const c = await browser.newContext(); const p = await c.newPage(); p.setDefaultTimeout(6000); p.on('dialog', (d) => d.accept()); return p; };
const has = (p, text) => p.getByText(text, { exact: false }).first().waitFor();
const login = async (p, id, pw = PW) => { await p.goto(BASE + '/login'); await p.getByLabel(/학번/).fill(id); await p.getByLabel('비밀번호').fill(pw); await p.getByRole('button', { name: '로그인' }).click(); };
const slot = async (p, date, start, end) => {
  await p.locator('input[type=date]').fill(date);
  await p.getByLabel('시작').selectOption(start);
  await p.getByLabel('종료').selectOption(end);
};
const seat = (p, no) => p.getByRole('button', { name: new RegExp('^' + no + ' ') });
const waitSeats = (p) => p.locator('.seat').first().waitFor();

const A = await newPage(), B = await newPage(), C = await newPage(), ADM = await newPage(), N = await newPage(), D = await newPage();

console.log('--- (1) US-01');
await check('/ 접근 시 로그인 화면으로 이동', async () => { await N.goto(BASE + '/'); await N.waitForURL('**/login'); });
await check('AC3 약한 비밀번호 거부 + 보완 항목 안내', async () => {
  await N.goto(BASE + '/signup'); await N.getByLabel('학번').fill('20269998'); await N.getByLabel('학교 이메일').fill('w@univ.ac.kr'); await N.getByLabel('비밀번호').fill('1234');
  await N.getByRole('button', { name: '가입하기' }).click(); await has(N, '보완할 항목');
});
await check('AC4 학번 중복 안내', async () => {
  await N.getByLabel('학번').fill('20260001'); await N.getByLabel('비밀번호').fill(PW); await N.getByRole('button', { name: '가입하기' }).click(); await has(N, '이미 사용 중인 아이디입니다.');
});
await check('AC1 가입 성공 → 인증 화면', async () => {
  await N.getByLabel('학번').fill('20269999'); await N.getByLabel('학교 이메일').fill('new@univ.ac.kr'); await N.getByRole('button', { name: '가입하기' }).click(); await N.waitForURL('**/verify-email'); await has(N, '회원가입이 완료되었습니다');
});
await check('AC2 인증 전 로그인 → 이메일 인증 안내 + 링크', async () => {
  await login(N, '20269999'); await has(N, '이메일 인증이 필요합니다'); await has(N, '이메일 인증하러 가기');
});
await check('AC5 잘못된 토큰 → 오류', async () => {
  await N.goto(BASE + '/verify-email'); await N.getByLabel('인증 토큰').fill('wrong-token'); await N.getByRole('button', { name: '인증하기' }).click(); await has(N, '유효하지 않은 인증 토큰입니다.');
});
await new Promise((r) => setTimeout(r, 1000)); // 로그가 파일에 기록될 시간
const token = [...readLog().matchAll(/이메일 인증 토큰: (\S+)/g)].pop()?.[1];
if (!token) {
  console.error(`로그에서 '이메일 인증 토큰'을 찾지 못했습니다: ${LOG}\n이 로그가 지금 실행 중인 bootRun의 로그인지 확인하세요.`);
  await browser.close();
  process.exit(1);
}
await check('AC2 올바른 토큰 → 인증 완료', async () => {
  await N.getByLabel('인증 토큰').fill(token); await N.getByRole('button', { name: '인증하기' }).click(); await has(N, '이메일 인증이 완료되었습니다.');
});
await check('AC5 같은 토큰 재사용 → 오류', async () => {
  await N.goto(BASE + '/verify-email?token=' + token); await N.getByRole('button', { name: '인증하기' }).click(); await has(N, '유효하지 않은 인증 토큰입니다.');
});

console.log('--- (2) US-02');
await check('AC2 틀린 비밀번호 / 없는 ID 동일 메시지', async () => {
  await login(N, '20260001', 'wrong'); await has(N, '아이디 또는 비밀번호가 올바르지 않습니다.');
  await login(N, 'nobody', 'wrong'); await has(N, '아이디 또는 비밀번호가 올바르지 않습니다.');
});
await check('AC2 PENDING 계정(20260005) 인증 안내', async () => { await login(N, '20260005'); await has(N, '이메일 인증이 필요합니다'); });
await check('AC1 학생A 로그인 → 좌석 화면', async () => { await login(A, '20260001'); await A.waitForURL('**/seats'); await waitSeats(A); });
await check('AC3 비로그인 /seats 접근 → 로그인 화면', async () => { await N.goto(BASE + '/seats'); await N.waitForURL('**/login'); });
await check('AC4 학생이 /admin/policies 접근 → 권한 없음', async () => { await A.goto(BASE + '/admin/policies'); await A.waitForURL('**/403'); await has(A, '접근 권한이 없습니다'); await A.goto(BASE + '/seats'); await waitSeats(A); });

console.log('--- (3) US-05');
await check('AC1 정책 안내 + 20석 그리드', async () => { await has(A, '평상시 정책'); await has(A, '최대 이용 시간 4시간'); if ((await A.locator('.seat').count()) !== 20) throw new Error('좌석 수 ' + await A.locator('.seat').count()); });
await check('AC2 11-20 10~12: A-02 예약됨 + 선택 시 안내, 예약 버튼 없음', async () => {
  await slot(A, TMR, '10:00', '12:00'); await A.waitForTimeout(500);
  if (!(await seat(A, 'A-02').getAttribute('class')).includes('seat--reserved')) throw new Error('A-02 not reserved');
  await seat(A, 'A-02').click(); await has(A, '이미 예약된 좌석입니다. 다른 좌석을 선택해 주세요.');
  if (await A.getByRole('button', { name: '예약하기' }).count()) throw new Error('예약 버튼이 보임');
});
await check('AC2 11-19 20~22: A-03 사용 중 + 안내', async () => {
  await slot(A, TODAY, HOUR, HOUR2); await A.waitForTimeout(500);
  if (!(await seat(A, 'A-03').getAttribute('class')).includes('seat--in_use')) throw new Error('A-03 not in_use');
  await seat(A, 'A-03').click(); await has(A, '사용 중인 좌석입니다.');
});
await check('AC4 예약 가능 기간 제한: 날짜 입력 max = +7일', async () => {
  // 예약 가능 기간은 서버의 실제 날짜 기준이므로 이 점검만 실행일 기준으로 계산한다
  const max = await A.locator('input[type=date]').getAttribute('max'); const exp = fmt(new Date(now.getTime() + 7 * 86400000));
  if (max !== exp) throw new Error(`max=${max} expected ${exp}`);
});

console.log('--- (4) US-06');
await check('AC1 학생A 11-20 10~12 A-01 예약 → 확인 + 내 예약 색', async () => {
  await slot(A, TMR, '10:00', '12:00'); await A.waitForTimeout(400); await seat(A, 'A-01').click();
  await A.getByRole('button', { name: '예약하기' }).click(); await has(A, '예약이 완료되었습니다. A-01');
  await A.waitForTimeout(600); if (!(await seat(A, 'A-01').getAttribute('class')).includes('seat--mine')) throw new Error('A-01 not mine');
});
await check('AC2 같은 시간대 겹침 예약 → 거부(UI에서 A-04 선택)', async () => {
  await slot(A, TMR, '11:00', '13:00'); await A.waitForTimeout(400); await seat(A, 'A-04').click(); await A.getByRole('button', { name: '예약하기' }).click(); await has(A, '같은 시간대에 이미 예약이 있습니다.');
});
await check('AC3 학생C(이용 제한) 예약 → 해제일 안내', async () => {
  await login(C, '20260003'); await C.waitForURL('**/seats'); await waitSeats(C); await slot(C, TMR, '10:00', '12:00'); await C.waitForTimeout(400);
  await seat(C, 'C-05').click(); await C.getByRole('button', { name: '예약하기' }).click(); await has(C, '이용 제한 중입니다.'); await has(C, '이후에 예약할 수 있습니다.');
});
await check('AC4/US-05 AC3 학생B 예약 → 학생A 새로고침 시 B-04 예약됨', async () => {
  await login(B, '20260002'); await B.waitForURL('**/seats'); await waitSeats(B); await slot(B, TMR, '14:00', '16:00'); await B.waitForTimeout(400);
  await seat(B, 'B-04').click(); await B.getByRole('button', { name: '예약하기' }).click(); await has(B, '예약이 완료되었습니다. B-04');
  await A.reload(); await waitSeats(A); await slot(A, TMR, '14:00', '16:00'); await A.waitForTimeout(500);
  if (!(await seat(A, 'B-04').getAttribute('class')).includes('seat--reserved')) throw new Error('B-04 not reserved on A');
});

console.log('--- (5) US-07');
await check('관리자 로그인 → 정책 관리 화면', async () => { await login(ADM, 'admin'); await ADM.waitForURL('**/admin/policies'); await has(ADM, '시험 기간'); });
await check('AC4 시작일>종료일 거부', async () => {
  const f = ADM.locator('form.inline-form'); await f.getByLabel('시작일').fill(TMR); await f.getByLabel('종료일').fill(TODAY); await f.getByRole('button', { name: '등록' }).click(); await has(ADM, '시험 기간의 시작일은 종료일보다 늦을 수 없습니다.');
});
await check('AC1 시험 기간 11-19~11-24 등록', async () => {
  const f = ADM.locator('form.inline-form'); await f.getByLabel('시작일').fill(TODAY); await f.getByLabel('종료일').fill(D5); await f.getByRole('button', { name: '등록' }).click(); await has(ADM, '등록했습니다.');
});
await check('AC3 학생 화면: 시험 기간 정책 + 최대 3시간 명시', async () => {
  await A.reload(); await waitSeats(A); await has(A, '시험 기간 정책 적용 중'); await has(A, '최대 이용 시간 3시간');
  const opts = await A.getByLabel('종료').locator('option').allTextContents();
  if (opts.length > 3) throw new Error('종료 선택지가 3시간 초과: ' + opts);
});
await check('AC5 EXAM_MAX_HOURS=2 저장 → 학생 화면 반영, 기존 예약(A-01 2시간) 유지', async () => {
  const row = ADM.locator('tr', { hasText: 'EXAM_MAX_HOURS' }); await row.locator('input').fill('2'); await row.getByRole('button', { name: '저장' }).click(); await has(ADM, 'EXAM_MAX_HOURS 값을 2(으)로 변경했습니다.');
  await A.reload(); await waitSeats(A); await has(A, '최대 이용 시간 2시간');
  await A.goto(BASE + '/reservations'); await A.locator('tr', { hasText: 'A-01' }).getByText('예약중').waitFor();
  await row.locator('input').fill('3'); await row.getByRole('button', { name: '저장' }).click(); await has(ADM, 'EXAM_MAX_HOURS 값을 3(으)로 변경했습니다.');
});
await check('시험 기간 삭제 → 평상시 복귀', async () => {
  await ADM.locator('tr', { hasText: D5 }).getByRole('button', { name: '삭제' }).click(); await has(ADM, '시험 기간을 삭제했습니다.');
  await A.goto(BASE + '/seats'); await waitSeats(A); await has(A, '평상시 정책');
});

console.log('--- (6) US-08');
await check('AC1 내 예약 최신순 4건(예약중/노쇼/취소됨/체크인 완료)', async () => {
  await A.goto(BASE + '/reservations'); await A.locator('tbody tr').first().waitFor();
  const rows = await A.locator('tbody tr').allTextContents(); if (rows.length !== 4) throw new Error('rows=' + rows.length);
  if (!rows[0].includes('A-01') || !rows[0].includes('예약중')) throw new Error('first: ' + rows[0]);
});
await check('AC2 필터 노쇼 → B-03만', async () => {
  await A.getByLabel('상태').selectOption({ label: '노쇼' }); await A.waitForTimeout(600); const rows = await A.locator('tbody tr').allTextContents(); if (rows.length !== 1 || !rows[0].includes('B-03')) throw new Error(rows.join('|'));
  await A.getByLabel('상태').selectOption({ label: '전체' }); await A.waitForTimeout(500);
});
await check('AC3·4 A-01 취소(일정 변경) → 취소됨 + 사유 기록', async () => {
  await A.locator('tr', { hasText: 'A-01' }).getByRole('button', { name: '취소' }).click(); await A.getByLabel('일정 변경').check(); await A.getByRole('button', { name: '예약 취소' }).click();
  await A.locator('tr', { hasText: 'A-01' }).getByText('취소됨').waitFor(); await A.locator('tr', { hasText: 'A-01' }).getByText('일정 변경').waitFor();
});
await check('AC4 좌석 즉시 예약 가능 복귀', async () => { await A.goto(BASE + '/seats'); await waitSeats(A); await slot(A, TMR, '10:00', '12:00'); await A.waitForTimeout(500); if (!(await seat(A, 'A-01').getAttribute('class')).includes('seat--available')) throw new Error('A-01 not available'); });
await check('AC5 취소됨·체크인 완료 행에는 취소 버튼 없음', async () => {
  await A.goto(BASE + '/reservations'); await A.locator('tbody tr').first().waitFor();
  for (const s of ['A-01', 'B-01']) if (await A.locator('tr', { hasText: s }).getByRole('button', { name: '취소' }).count()) throw new Error(s + ' has cancel button');
});

console.log('--- (7) US-04');
await check('AC2 진행 중 예약·이용 제한 학생C 탈퇴 → 거부 + 계정 유지', async () => {
  await C.goto(BASE + '/account'); await C.getByRole('button', { name: '회원 탈퇴' }).click(); await has(C, '진행 중인 예약 또는 패널티가 존재하여 탈퇴할 수 없습니다');
  if (!C.url().includes('/account')) throw new Error('이동함: ' + C.url());
});
await check('AC1 학생D 탈퇴 → 로그인 화면', async () => {
  await login(D, '20260004'); await D.waitForURL('**/seats'); await D.getByRole('link', { name: '내 계정' }).click(); await D.getByRole('button', { name: '회원 탈퇴' }).click(); await D.waitForURL('**/login');
});
await check('AC3 탈퇴 계정 재로그인 거부', async () => { await login(D, '20260004'); await has(D, '아이디 또는 비밀번호가 올바르지 않습니다.'); });
await check('로그아웃 → /seats 접근 시 로그인 화면', async () => {
  await A.goto(BASE + '/seats'); await waitSeats(A); await A.getByRole('button', { name: '로그아웃' }).click(); await A.waitForURL('**/login'); await A.goto(BASE + '/seats'); await A.waitForURL('**/login');
});
await check('만료/삭제된 토큰으로 API 호출 시 로그인 화면(401 처리)', async () => {
  await login(A, '20260001'); await A.waitForURL('**/seats'); await A.evaluate(() => localStorage.setItem('lsrs.accessToken', 'a.b.c')); await A.goto(BASE + '/reservations'); await A.waitForURL('**/login');
});

console.log(`\nRESULT pass=${pass} fail=${fail}`);
await browser.close();
