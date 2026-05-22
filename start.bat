@echo off
REM Docker Compose 快速启动脚本 (Windows)

echo ========================================
echo   Yu Picture Backend Docker 部署
echo ========================================
echo.

REM 检查 Docker 是否安装
docker --version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到 Docker，请先安装 Docker Desktop
    pause
    exit /b 1
)

REM 检查 Docker Compose 是否安装
docker-compose --version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到 Docker Compose，请确保 Docker Desktop 已正确安装
    pause
    exit /b 1
)

echo [1/3] 检查环境变量配置...
if not exist .env (
    echo [提示] 未找到 .env 文件，将使用默认配置
    echo [提示] 如需自定义配置，请复制 .env.example 为 .env 并修改
) else (
    echo [成功] 找到 .env 配置文件
)

echo.
echo [2/3] 构建并启动服务...
docker-compose up -d --build

if errorlevel 1 (
    echo.
    echo [错误] 服务启动失败，请检查日志
    docker-compose logs
    pause
    exit /b 1
)

echo.
echo [3/3] 等待服务启动...
timeout /t 10 /nobreak >nul

echo.
echo ========================================
echo   部署完成！
echo ========================================
echo.
echo 服务访问地址：
echo   - API 文档: http://localhost:8123/api/doc.html
echo   - 健康检查: http://localhost:8123/api/health
echo.
echo 常用命令：
echo   - 查看日志: docker-compose logs -f
echo   - 停止服务: docker-compose down
echo   - 重启服务: docker-compose restart
echo.
echo 查看服务状态：
docker-compose ps

pause
