$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$minioExe = Join-Path $root "minio.exe"
$dataDir = Join-Path $root "data"

if (-not (Test-Path $minioExe)) {
    Write-Error "找不到 minio.exe，请先下载到: $minioExe"
}

New-Item -ItemType Directory -Force -Path $dataDir | Out-Null

$env:MINIO_ROOT_USER = "minioadmin"
$env:MINIO_ROOT_PASSWORD = "minioadmin"

Write-Host "MinIO 数据目录: $dataDir"
Write-Host "API 地址: http://localhost:9000"
Write-Host "控制台: http://localhost:9001"
Write-Host "账号/密码: minioadmin / minioadmin"
Write-Host "按 Ctrl+C 停止服务"
Write-Host ""

& $minioExe server $dataDir --console-address ":9001"
