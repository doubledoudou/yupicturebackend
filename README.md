# Docker 快速开始

## 前置要求

- 安装 [Docker Desktop](https://www.docker.com/products/docker-desktop/)（包含 Docker 和 Docker Compose）

## 快速启动

### Windows 用户

双击运行 `start.bat` 文件，或在命令行执行：

```bash
start.bat
```

### Linux/Mac 用户

赋予执行权限并运行：

```bash
chmod +x start.sh
./start.sh
```

### 手动启动

如果不想使用脚本，可以手动执行：

```bash
# 1. 复制环境变量配置文件（可选）
cp .env.example .env

# 2. 编辑 .env 文件，填入你的配置（如腾讯云 COS 信息）

# 3. 构建并启动所有服务
docker-compose up -d --build
```

## 访问应用

等待约 1-2 分钟后，访问：

- **API 文档**: http://localhost:8123/api/doc.html
- **健康检查**: http://localhost:8123/api/health

## 常用操作

### 查看服务状态

```bash
docker-compose ps
```

### 查看日志

```bash
# 查看所有服务日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f app
docker-compose logs -f mysql
docker-compose logs -f redis
```

### 停止服务

```bash
docker-compose down
```

### 重启服务

```bash
docker-compose restart
```

### 重新构建并启动

```bash
docker-compose up -d --build
```

### 进入容器

```bash
# 进入应用容器
docker exec -it yu-picture-app sh

# 进入 MySQL 容器
docker exec -it yu-picture-mysql mysql -uroot -plirong

# 进入 Redis 容器
docker exec -it yu-picture-redis redis-cli
```

## 配置说明

### 环境变量配置

复制 `.env.example` 为 `.env` 并修改以下配置：

```env
# 腾讯云 COS 配置（必填）
COS_CLIENT_HOST=your-cos-host
COS_CLIENT_SECRET_ID=your-secret-id
COS_CLIENT_SECRET_KEY=your-secret-key
COS_CLIENT_REGION=your-region
COS_CLIENT_BUCKET=your-bucket
```

### 端口映射

- 应用服务: `8123:8123`
- MySQL: `3306:3306`
- Redis: `6379:6379`

如需修改端口，请编辑 `docker-compose.yml` 文件。

## 数据持久化

以下数据通过 Docker Volume 持久化，删除容器后数据不会丢失：

- MySQL 数据: `mysql_data`
- Redis 数据: `redis_data`
- 应用日志: `app_logs`

## 故障排查

### 应用启动失败

1. 查看应用日志：
   ```bash
   docker-compose logs -f app
   ```

2. 确认 MySQL 和 Redis 已启动：
   ```bash
   docker-compose ps
   ```

3. 检查数据库初始化：
   ```bash
   docker-compose logs mysql
   ```

### 端口冲突

如果端口被占用，修改 `docker-compose.yml` 中的端口映射：

```yaml
ports:
  - "8124:8123"  # 将外部端口改为 8124
```

### 数据库连接失败

等待 MySQL 完全启动（通常需要 30-60 秒），应用会自动重试连接。

## 安全提示

⚠️ **重要**：在生产环境中，请务必：

1. 修改 MySQL 和 Redis 的默认密码
2. 不要将 `.env` 文件提交到版本控制系统
3. 使用 HTTPS 反向代理（如 Nginx）
4. 限制数据库和 Redis 的外部访问

## 更多信息

详细的部署文档请参考 [DEPLOYMENT.md](DEPLOYMENT.md)
