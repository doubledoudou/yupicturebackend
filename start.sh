#!/bin/bash

# Docker Compose 快速启动脚本 (Linux/Mac)

echo "========================================"
echo "  Yu Picture Backend Docker 部署"
echo "========================================"
echo ""

# 检查 Docker 是否安装
if ! command -v docker &> /dev/null; then
    echo "[错误] 未检测到 Docker，请先安装 Docker"
    exit 1
fi

# 检查 Docker Compose 是否安装
if ! command -v docker-compose &> /dev/null; then
    echo "[错误] 未检测到 Docker Compose，请确保已正确安装"
    exit 1
fi

echo "[1/3] 检查环境变量配置..."
if [ ! -f .env ]; then
    echo "[提示] 未找到 .env 文件，将使用默认配置"
    echo "[提示] 如需自定义配置，请复制 .env.example 为 .env 并修改"
else
    echo "[成功] 找到 .env 配置文件"
fi

echo ""
echo "[2/3] 构建并启动服务..."
docker-compose up -d --build

if [ $? -ne 0 ]; then
    echo ""
    echo "[错误] 服务启动失败，请检查日志"
    docker-compose logs
    exit 1
fi

echo ""
echo "[3/3] 等待服务启动..."
sleep 10

echo ""
echo "========================================"
echo "  部署完成！"
echo "========================================"
echo ""
echo "服务访问地址："
echo "  - API 文档: http://localhost:8123/api/doc.html"
echo "  - 健康检查: http://localhost:8123/api/health"
echo ""
echo "常用命令："
echo "  - 查看日志: docker-compose logs -f"
echo "  - 停止服务: docker-compose down"
echo "  - 重启服务: docker-compose restart"
echo ""
echo "查看服务状态："
docker-compose ps
