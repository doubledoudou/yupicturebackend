# Docker 部署快速参考

## 🚀 一键启动

```bash
# Windows
start.bat

# Linux/Mac
chmod +x start.sh && ./start.sh
```

## 📋 常用命令速查

### 服务管理

```bash
# 启动服务
docker-compose up -d

# 停止服务
docker-compose down

# 重启服务
docker-compose restart

# 重新构建并启动
docker-compose up -d --build
```

### 查看状态

```bash
# 查看所有容器状态
docker-compose ps

# 查看应用日志
docker-compose logs -f app

# 查看 MySQL 日志
docker-compose logs -f mysql

# 查看 Redis 日志
docker-compose logs -f redis
```

### 进入容器

```bash
# 进入应用容器
docker exec -it yu-picture-app sh

# 进入 MySQL
docker exec -it yu-picture-mysql mysql -uroot -plirong

# 进入 Redis
docker exec -it yu-picture-redis redis-cli
```

### 数据管理

```bash
# 查看数据卷
docker volume ls

# 备份 MySQL 数据
docker exec yu-picture-mysql mysqldump -uroot -plirong yu_picture > backup.sql

# 恢复 MySQL 数据
docker exec -i yu-picture-mysql mysql -uroot -plirong yu_picture < backup.sql
```

### 清理操作

```bash
# 停止并删除容器（保留数据）
docker-compose down

# 停止并删除容器和数据卷（⚠️ 数据会丢失）
docker-compose down -v

# 清理未使用的镜像
docker image prune -a
```

## 🔗 访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| API 文档 | http://localhost:8123/api/doc.html | Knife4j 接口文档 |
| 健康检查 | http://localhost:8123/api/health | 应用健康状态 |
| MySQL | localhost:3306 | 数据库连接 |
| Redis | localhost:6379 | 缓存连接 |

## 🔑 默认账号密码

| 服务 | 用户名 | 密码 |
|------|--------|------|
| MySQL | root | lirong |
| Redis | - | 无密码 |

⚠️ **生产环境务必修改默认密码！**

## 📝 配置文件

| 文件 | 用途 |
|------|------|
| `.env` | 环境变量配置（需从 .env.example 复制） |
| `docker-compose.yml` | 服务编排配置 |
| `Dockerfile` | 应用镜像构建 |
| `application-prod.yml` | 生产环境配置 |

## 🐛 常见问题

### 1. 端口被占用

**症状**: 启动时提示端口已被使用

**解决**: 修改 `docker-compose.yml` 中的端口映射
```yaml
ports:
  - "8124:8123"  # 改为其他端口
```

### 2. 应用启动失败

**检查步骤**:
```bash
# 1. 查看日志
docker-compose logs -f app

# 2. 确认依赖服务正常
docker-compose ps

# 3. 检查数据库是否就绪
docker-compose logs mysql
```

### 3. 数据库连接失败

**原因**: MySQL 尚未完全启动

**解决**: 等待 30-60 秒，应用会自动重试

### 4. 内存不足

**症状**: 容器频繁重启

**解决**: 调整 JVM 参数（Dockerfile）
```dockerfile
ENV JAVA_OPTS="-Xms256m -Xmx512m ..."
```

## 📊 监控命令

```bash
# 查看容器资源使用
docker stats

# 查看磁盘使用
docker system df

# 查看特定容器资源
docker stats yu-picture-app
```

## 🔄 更新流程

```bash
# 1. 拉取最新代码
git pull

# 2. 停止旧服务
docker-compose down

# 3. 重新构建并启动
docker-compose up -d --build

# 4. 查看日志确认启动成功
docker-compose logs -f app
```

## 💡 实用技巧

### 1. 后台运行
```bash
docker-compose up -d  # -d 表示后台运行
```

### 2. 只启动特定服务
```bash
docker-compose up -d mysql redis  # 只启动 MySQL 和 Redis
```

### 3. 实时查看日志
```bash
docker-compose logs -f --tail=100 app  # 查看最后 100 行并持续跟踪
```

### 4. 执行一次性命令
```bash
# 在容器中执行命令
docker-compose exec app java -version

# 进入交互式 shell
docker-compose exec app sh
```

### 5. 导出容器日志
```bash
docker-compose logs app > app.log 2>&1
```

## 🎯 快速诊断脚本

```bash
# 创建诊断脚本 check.sh
#!/bin/bash
echo "=== 服务状态 ==="
docker-compose ps

echo -e "\n=== 网络检查 ==="
docker network inspect yu-picture-backend_yu-picture-network --format='{{range .Containers}}{{.Name}} {{end}}'

echo -e "\n=== 数据卷 ==="
docker volume ls | grep yu-picture

echo -e "\n=== 最近日志 ==="
docker-compose logs --tail=20 app
```

---

**提示**: 将此文件保存为 `QUICK-REFERENCE.md`，方便随时查阅！
