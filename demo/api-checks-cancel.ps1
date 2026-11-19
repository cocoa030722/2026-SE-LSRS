# 예약 취소 예외 케이스 3개만 확인하는 데모 스크립트 (US-08 AC5)
#
# 안전한 이유: 3개 모두 "거부"를 기대하는 요청이다. 거부되면 DB가 바뀌지 않는다.
#   시험 기간 같은 임시 데이터를 만들지 않으므로 몇 번을 실행해도 결과가 같다.
# 전제: seed-demo.sql 실행 직후 상태, 백엔드 기동 중
#   예약 ID(seed 기준): 1=학생B 예약중, 3=학생A 체크인 완료, 4=학생A 취소됨
# 실행: powershell -ExecutionPolicy Bypass -File demo\api-checks-cancel.ps1
#       (다른 주소: -Base http://localhost:8081)

param(
    [string]$Base = 'http://localhost:8080',
    [string]$Password = 'Passw0rd!'
)

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

# 응답 상태 코드와 본문을 돌려준다. 4xx/5xx 도 예외 없이 처리한다.
function Invoke-Api {
    param([string]$Method, [string]$Path, [string]$Token, $Body)
    $headers = @{}
    if ($Token) { $headers['Authorization'] = "Bearer $Token" }
    $args = @{ Uri = "$Base$Path"; Method = $Method; Headers = $headers; UseBasicParsing = $true }
    if ($null -ne $Body) {
        $args['ContentType'] = 'application/json; charset=utf-8'
        $args['Body'] = [System.Text.Encoding]::UTF8.GetBytes(($Body | ConvertTo-Json -Compress))
    }
    try {
        $r = Invoke-WebRequest @args
        return [pscustomobject]@{ Status = [int]$r.StatusCode; Body = $r.Content }
    } catch {
        $resp = $_.Exception.Response
        if ($null -eq $resp) { throw }
        $reader = New-Object System.IO.StreamReader($resp.GetResponseStream(), [System.Text.Encoding]::UTF8)
        return [pscustomobject]@{ Status = [int]$resp.StatusCode; Body = $reader.ReadToEnd() }
    }
}

$login = Invoke-Api POST '/api/auth/login' $null @{ loginId = '20260001'; password = $Password }
if ($login.Status -ne 200) { throw "로그인 실패: $($login.Status) $($login.Body)" }
$studentA = ($login.Body | ConvertFrom-Json).accessToken

$pass = 0
$fail = 0
$changed = $false
$reason = @{ reason = 'SCHEDULE_CHANGE' }

$cases = @(
    @{ Name = '학생A가 학생B의 예약 1번 취소 → 존재 여부 비노출'; Id = 1; Expect = 404 },
    @{ Name = '이미 취소된 예약 4번 취소';                        Id = 4; Expect = 409 },
    @{ Name = '체크인 완료된 예약 3번 취소';                      Id = 3; Expect = 409 }
)

Write-Host "`n=== US-08 AC5: 취소 거부 케이스 3개 ==="
foreach ($c in $cases) {
    $r = Invoke-Api PUT "/api/reservations/$($c.Id)/cancel" $studentA $reason
    $ok = ($r.Status -eq $c.Expect)
    if ($ok) { $pass++ } else { $fail++ }
    # 2xx 이면 서버가 요청을 받아들여 데이터가 바뀐 것이다.
    if ($r.Status -ge 200 -and $r.Status -lt 300) { $changed = $true }
    Write-Host ("{0} {1}  (기대 {2} / 실제 {3})" -f $(if ($ok) { 'PASS' } else { 'FAIL' }), $c.Name, $c.Expect, $r.Status)
    Write-Host ("     응답: {0}" -f $r.Body)
}

Write-Host ("`nRESULT pass={0} fail={1}" -f $pass, $fail)
if ($changed) {
    Write-Host "경고: 취소 요청이 성공해 데이터가 바뀌었습니다. seed-demo.sql 을 다시 실행하세요."
}
if ($fail -gt 0) { exit 1 }
