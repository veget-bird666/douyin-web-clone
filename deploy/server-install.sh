#!/bin/bash
set -euo pipefail

# 在服务器上执行：bash deploy/server-install.sh

echo "==> 安装 Docker（Ubuntu 22.04）"
if ! command -v docker >/dev/null 2>&1; then
  sudo apt update
  sudo apt install -y docker.io docker-compose
  sudo systemctl enable --now docker
fi

docker --version
docker-compose --version

cd "$(dirname "$0")/.."

if [ ! -f .env ]; then
  echo "请先创建 .env：cp .env.example .env && nano .env"
  exit 1
fi

echo "==> 构建并启动容器"
sudo docker-compose up -d --build

echo "==> 容器状态"
sudo docker-compose ps

echo ""
echo "完成。下一步在宿主机 Nginx 配置主域名反代到 127.0.0.1:18080"
echo "参考：deploy/host-nginx/douyin.conf.example"
