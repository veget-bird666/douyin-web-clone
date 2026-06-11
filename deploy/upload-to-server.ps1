# 在本机 Windows PowerShell 执行，把项目上传到服务器
# 用法: .\deploy\upload-to-server.ps1 -ServerIP "你的公网IP"
# 说明: 不上传 node_modules、target 等，服务器上 Docker 构建时会自动安装依赖

param(
    [Parameter(Mandatory = $true)]
    [string]$ServerIP,

    [string]$User = "root",
    [string]$RemoteDir = "/opt/douyin-web-clone"
)

$ErrorActionPreference = "Stop"
$ProjectRoot = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$Archive = Join-Path $env:TEMP "douyin-deploy.tar.gz"

Write-Host "打包项目（排除 node_modules、target、dist）..."
if (Test-Path $Archive) { Remove-Item $Archive -Force }

Push-Location $ProjectRoot
try {
    tar -czf $Archive `
        --exclude="vue/node_modules" `
        --exclude="vue/dist" `
        --exclude="springboot/target" `
        --exclude="tools/minio/data" `
        --exclude=".git" `
        springboot vue deploy docker-compose.yml douyin_schema.sql .env.example .dockerignore
}
finally {
    Pop-Location
}

$sizeMb = [math]::Round((Get-Item $Archive).Length / 1MB, 1)
Write-Host "压缩包大小: ${sizeMb} MB"
Write-Host "上传到 ${User}@${ServerIP}:${RemoteDir}"

ssh "${User}@${ServerIP}" "mkdir -p ${RemoteDir}"
scp $Archive "${User}@${ServerIP}:/tmp/douyin-deploy.tar.gz"
ssh "${User}@${ServerIP}" "tar -xzf /tmp/douyin-deploy.tar.gz -C ${RemoteDir} && rm -f /tmp/douyin-deploy.tar.gz"

Remove-Item $Archive -Force -ErrorAction SilentlyContinue

Write-Host ""
Write-Host "上传完成。SSH 登录服务器后执行:"
Write-Host "  cd $RemoteDir"
Write-Host "  cp .env.example .env"
Write-Host "  nano .env"
Write-Host "  bash deploy/server-install.sh"
