$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$minioExe = Join-Path $root "minio.exe"
$dataDir = Join-Path $root "data"

if (-not (Test-Path $minioExe)) {
    Write-Error "minio.exe not found. Run download-minio.ps1 first: $minioExe"
}

New-Item -ItemType Directory -Force -Path $dataDir | Out-Null

$env:MINIO_ROOT_USER = "minioadmin"
$env:MINIO_ROOT_PASSWORD = "minioadmin"

Write-Host "Data dir: $dataDir"
Write-Host "API:      http://localhost:9000"
Write-Host "Console:  http://localhost:9001"
Write-Host "User/Pwd: minioadmin / minioadmin"
Write-Host "Press Ctrl+C to stop"
Write-Host ""

& $minioExe server $dataDir --console-address ":9001"
