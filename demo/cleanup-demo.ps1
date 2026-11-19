<#
.SYNOPSIS
  Sprint 1 데모가 끝난 뒤 데모용 임시 데이터와 파일을 정리한다.

.DESCRIPTION
  1. 데모 DB: demo\cleanup-demo.sql 실행
     - Seed 계정·좌석·예약, 데모 중 가입·예약·취소·탈퇴 데이터, 시험 기간, 인증 토큰 삭제
     - 정책 파라미터를 기본값으로 되돌림
  2. 파일: 저장소 루트의 boot.log(bootRun 로그, 이메일 인증 토큰 포함) 삭제
  3. -Rehearsal: demo\rehearsal\node_modules 도 삭제 (리허설 스크립트 의존성)

  리허설 사이에 Seed 상태로 되돌릴 때는 이 스크립트가 아니라 demo\seed-demo.sql 을 실행한다.

.EXAMPLE
  .\demo\cleanup-demo.ps1
  .\demo\cleanup-demo.ps1 -Force -Rehearsal
#>
param(
    # MySQL 컨테이너 이름. 생략하면 compose 의 mysql 서비스 컨테이너를 찾는다.
    [string]$Container,
    # 확인 질문 없이 실행
    [switch]$Force,
    # demo\rehearsal\node_modules 도 삭제
    [switch]$Rehearsal,
    # DB 정리를 건너뛰고 파일만 정리
    [switch]$SkipDb
)

# 네이티브 명령(docker)의 stderr 로 스크립트가 중단되지 않도록 Continue 로 두고 종료 코드를 직접 확인한다 (PowerShell 5.1)
$ErrorActionPreference = 'Continue'
$RepoRoot = Split-Path -Parent $PSScriptRoot

if (-not $SkipDb) {
    if (-not $Container) {
        Push-Location $RepoRoot
        try { $Container = (docker compose ps --format '{{.Name}}' mysql | Select-Object -First 1) } finally { Pop-Location }
        if (-not $Container) { $Container = '2026-se-lsrs-mysql-1' }
    }
    $running = docker ps --filter "name=^$Container$" --format '{{.Names}}'
    if (-not $running) {
        Write-Host "MySQL 컨테이너 '$Container'가 실행 중이 아닙니다. Docker Desktop과 bootRun(또는 docker compose up -d)을 확인하세요." -ForegroundColor Red
        exit 1
    }
}

Write-Host '정리 대상:'
if (-not $SkipDb) { Write-Host "  - DB($Container): 계정·좌석·예약·시험 기간·인증 토큰 전부 삭제, 정책 값 기본값 복원" }
Write-Host "  - 파일: $(Join-Path $RepoRoot 'boot.log')"
if ($Rehearsal) { Write-Host "  - 폴더: $(Join-Path $PSScriptRoot 'rehearsal\node_modules')" }

if (-not $Force) {
    $answer = Read-Host '되돌릴 수 없습니다. 계속할까요? (y/N)'
    if ($answer -notmatch '^[yY]') { Write-Host '취소했습니다.'; exit 0 }
}

if (-not $SkipDb) {
    Write-Host "`n[DB] cleanup-demo.sql 실행"
    # PowerShell 파이프(Get-Content | docker)는 콘솔 인코딩이 UTF-8이면 BOM을 붙여 mysql 이 1행에서 문법 오류를 낸다.
    # cmd 의 < 리다이렉트는 파일 바이트를 그대로 넘긴다. 경로 따옴표 문제를 피하려고 demo 폴더에서 상대 경로로 실행한다.
    # 비밀번호는 MYSQL_PWD 로 넘겨 'Using a password on the command line' 경고(stderr)를 피한다.
    Push-Location $PSScriptRoot
    try {
        cmd /c "docker exec -i -e MYSQL_PWD=lsrs $Container mysql -ulsrs --table lsrs < cleanup-demo.sql" |
            ForEach-Object { Write-Host "  $_" }
    } finally { Pop-Location }
    if ($LASTEXITCODE -ne 0) { Write-Host 'DB 정리에 실패했습니다.' -ForegroundColor Red; exit 1 }
}

$targets = @(Join-Path $RepoRoot 'boot.log')
if ($Rehearsal) { $targets += Join-Path $PSScriptRoot 'rehearsal\node_modules' }
foreach ($target in $targets) {
    if (Test-Path $target) {
        try {
            Remove-Item -Recurse -Force $target -ErrorAction Stop
            Write-Host "[파일] 삭제: $target"
        } catch {
            Write-Host "[파일] 삭제 실패(사용 중일 수 있음 - bootRun을 먼저 종료): $target" -ForegroundColor Yellow
        }
    } else {
        Write-Host "[파일] 없음: $target"
    }
}

Write-Host "`n정리 완료. 다시 시연하려면 demo\seed-demo.sql 을 실행하세요." -ForegroundColor Green
Write-Host '참고: 데모용 JWT 기본 키(application.properties, 커밋 0aeb98f)는 코드 변경이라 이 스크립트가 되돌리지 않습니다.'
