# Docker 部署指南

本文档介绍如何使用 Docker 容器化部署 Yu Picture Backend 项目。

## 前置要求

- 安装 Docker (版本 20.10+)
- 安装 Docker Compose (版本 2.0+)

## 部署步骤

### 1. 克隆项目代码

```bash
git clone <your-repository-url>
cd yu-picture-backend
```

### 2. 配置环境变量（可选）

如果需要自定义配置，可以复制 `.env.example` 文件为 `.env` 并修改相应值：

```bash
cp .env.example .env
```

编辑 `.env` 文件，填入你的腾讯云 COS 配置等信息。

### 3. 准备数据库初始化脚本

确保 `sql/create_table.sql` 文件包含正确的数据库表结构，该文件会在 MySQL 容器首次启动时自动执行。

### 4. 启动服务

使用 Docker Compose 启动所有服务：

```bash
docker-compose up -d
```

这将启动以下服务：
- MySQL 数据库 (端口 3306)
- Redis 缓存 (端口 6379)
- Spring Boot 应用 (端口 8123)

### 5. 验证部署

等待几分钟让应用完全启动，然后访问：

- API 文档: http://localhost:8123/api/doc.html
- 健康检查: http://localhost:8123/api/actuator/health

## 常用命令

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

### 进入容器内部
```bash
# 进入应用容器
docker exec -it yu-picture-app sh

# 进入 MySQL 容器
docker exec -it yu-picture-mysql mysql -uroot -plirong

# 进入 Redis 容器
docker exec -it yu-picture-redis redis-cli
```

## 数据持久化

Docker Compose 配置中已设置数据卷来持久化重要数据：

- `mysql_data`: MySQL 数据库文件
- `redis_data`: Redis 持久化数据
- `app_logs`: 应用日志

这些数据在容器删除后仍然保留。

## 安全建议

1. **修改默认密码**: 在生产环境中，务必修改 MySQL 和 Redis 的默认密码
2. **保护敏感信息**: 不要将 `.env` 文件提交到版本控制系统
3. **使用 HTTPS**: 在生产环境中配置反向代理（如 Nginx）来启用 HTTPS
4. **限制端口访问**: 根据需要限制外部对数据库和 Redis 端口的访问

## 故障排查

### 应用无法启动

1. 检查日志：
   ```bash
   docker-compose logs -f app
   ```

2. 确认 MySQL 和 Redis 服务正常运行：
   ```bash
   docker-compose ps
   ```

3. 检查网络连接：
   ```bash
   docker network ls
   docker inspect yu-picture-network
   ```

### 数据库连接失败

1. 确认 MySQL 容器已启动并健康
2. 检查数据库配置是否正确
3. 等待 MySQL 完全启动后再启动应用

### Redis 连接失败

1. 确认 Redis 容器已启动
2. 检查 Redis 配置和端口映射

## 生产环境优化

对于生产环境部署，建议：

1. 使用专门的 Docker 镜像仓库
2. 配置日志收集系统（如 ELK）
3. 设置监控和告警
4. 配置自动备份策略
5. 使用更强大的服务器资源
6. 配置负载均衡和高可用

## 更新应用

当代码有更新时：

```bash
# 拉取最新代码
git pull

# 重新构建并启动
docker-compose up -d --build
```
