$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$out = Join-Path $root "minio.exe"
$url = "https://dl.min.io/server/minio/release/windows-amd64/minio.exe"

Write-Host "Downloading MinIO to: $out"

$curl = Get-Command curl.exe -ErrorAction SilentlyContinue
if ($curl) {
    & curl.exe --ssl-no-revoke -L -o $out $url
} else {
    try {
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        Invoke-WebRequest -Uri $url -OutFile $out -UseBasicParsing
    } catch {
        Write-Error "Download failed. Try manually: $url"
    }
}

& $out --version
Write-Host "Done. Run start-minio.bat to start the server."
