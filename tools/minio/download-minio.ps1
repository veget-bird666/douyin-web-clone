$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$out = Join-Path $root "minio.exe"
$url = "https://dl.min.io/server/minio/release/windows-amd64/minio.exe"

Write-Host "正在下载 MinIO 到: $out"
Invoke-WebRequest -Uri $url -OutFile $out -UseBasicParsing

& $out --version
Write-Host "下载完成。运行 start-minio.bat 启动服务。"
