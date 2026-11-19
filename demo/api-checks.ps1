# 화면으로 보여 줄 수 없는 US-07·08 서버 검증을 API로 직접 호출하는 데모 스크립트
#
# 전제: seed-demo.sql 실행 직후 상태, 백엔드 기동 중 (기본 http://localhost:8080)
#   예약 ID(seed 기준): 1=학생B A-02 예약중, 3=학생A 체크인 완료, 4=학생A 취소됨
# 실행: powershell -ExecutionPolicy Bypass -File demo\api-checks.ps1
#       (다른 주소: -Base http://localhost:8081)

param(
    [string]$Base = 'http://localhost:8080',
    [string]$Password = 'Passw0rd!',
    [string]$Today = '2026-11-19',
    [string]$Tomorrow = '2026-11-20',
    [string]$OutOfRange = '2026-11-22',
    [string]$ExamEnd = '2026-11-24'
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

function Get-Token([string]$LoginId) {
    $r = Invoke-Api POST '/api/auth/login' $null @{ loginId = $LoginId; password = $Password }
    if ($r.Status -ne 200) { throw "로그인 실패($LoginId): $($r.Status) $($r.Body)" }
    return ($r.Body | ConvertFrom-Json).accessToken
}

$script:pass = 0
$script:fail = 0

# 기대한 상태 코드인지 확인하고, 서버가 준 응답 본문을 그대로 보여 준다 (데모 화면에 노출할 메시지)
function Assert-Api {
    param([string]$Name, [int]$Expect, $Response)
    $ok = ($Response.Status -eq $Expect)
    if ($ok) { $script:pass++ } else { $script:fail++ }
    $mark = if ($ok) { 'PASS' } else { 'FAIL' }
    Write-Host ("{0} {1}  (기대 {2} / 실제 {3})" -f $mark, $Name, $Expect, $Response.Status)
    Write-Host ("     응답: {0}" -f $Response.Body)
}

$admin = Get-Token 'admin'
$studentA = Get-Token '20260001'
$examId = $null

try {
    Write-Host "`n=== US-07 AC2: 시험 기간 정책 초과 요청 (화면은 선택지를 막아 보낼 수 없음) ==="
    $created = Invoke-Api POST '/api/admin/exam-periods' $admin @{ startDate = $Today; endDate = $ExamEnd }
    Assert-Api '시험 기간 11-19~11-24 등록' 201 $created
    if ($created.Status -eq 201) {
        $parsed = $created.Body | ConvertFrom-Json
        $examId = if ($parsed.examPeriodId) { $parsed.examPeriodId } else { $parsed.id }
    }

    # 학생A, A-05(seat_id=5) — 정책 검증이 좌석 충돌보다 먼저라 좌석은 무엇이든 무관하다
    $r = Invoke-Api POST '/api/reservations' $studentA @{ seatId = 5; startAt = "${Tomorrow}T10:00:00"; endAt = "${Tomorrow}T14:00:00" }
    Assert-Api '4시간 예약 → 최대 이용 시간 초과' 400 $r

    $r = Invoke-Api POST '/api/reservations' $studentA @{ seatId = 5; startAt = "${OutOfRange}T10:00:00"; endAt = "${OutOfRange}T11:00:00" }
    Assert-Api '예약 가능 기간(11-19~11-20) 밖 날짜 예약' 400 $r

    Write-Host "`n=== US-07 AC4: 겹치는 시험 기간 (화면에서도 가능하나 409 확인용) ==="
    $r = Invoke-Api POST '/api/admin/exam-periods' $admin @{ startDate = $ExamEnd; endDate = '2026-11-26' }
    Assert-Api '기존 기간과 하루라도 겹치면 거부 (양 끝 포함)' 409 $r
}
finally {
    if ($examId) {
        $d = Invoke-Api DELETE "/api/admin/exam-periods/$examId" $admin
        Write-Host ("`n[정리] 시험 기간 삭제 → {0}" -f $d.Status)
    } else {
        Write-Host "`n[정리] 삭제할 시험 기간 ID를 얻지 못했습니다. 관리자 화면에서 11-19~11-24 기간이 남았는지 확인하세요."
    }
}

Write-Host "`n=== US-08 AC5: 취소 거부 (화면은 타인 예약·취소 불가 예약에 버튼이 없음) ==="
$reason = @{ reason = 'SCHEDULE_CHANGE' }

$r = Invoke-Api PUT '/api/reservations/1/cancel' $studentA $reason
Assert-Api '학생A가 학생B의 예약 1번 취소 → 존재 여부 비노출' 404 $r

$r = Invoke-Api PUT '/api/reservations/4/cancel' $studentA $reason
Assert-Api '이미 취소된 예약 4번 취소' 409 $r

$r = Invoke-Api PUT '/api/reservations/3/cancel' $studentA $reason
Assert-Api '체크인 완료된 예약 3번 취소' 409 $r

Write-Host "`n=== US-06 AC4 (참고): 이미 예약된 좌석 A-02 (화면은 예약 버튼이 없음) ==="
$r = Invoke-Api POST '/api/reservations' $studentA @{ seatId = 2; startAt = "${Tomorrow}T10:00:00"; endAt = "${Tomorrow}T12:00:00" }
Assert-Api 'B가 예약한 좌석 A-02 예약 시도' 409 $r

Write-Host ("`nRESULT pass={0} fail={1}" -f $script:pass, $script:fail)
if ($script:fail -gt 0) { exit 1 }
