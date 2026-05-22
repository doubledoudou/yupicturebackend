# Yu Picture Backend

一个基于 Spring Boot 的图片管理后端系统,支持图片上传、存储、分类和管理功能。

## 🚀 技术栈

- **后端框架**: Spring Boot 2.7.6
- **数据库**: MySQL + MyBatis Plus
- **缓存**: Redis
- **对象存储**: 腾讯云 COS
- **接口文档**: Knife4j (Swagger UI)
- **构建工具**: Maven

## 📋 前置要求

- JDK 1.8+
- Maven 3.6+
- MySQL 5.7+
- Redis 5.0+

## ⚙️ 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/your-username/yu-picture-backend.git
cd yu-picture-backend
```

### 2. 配置环境变量

**重要**: 本项目使用环境变量管理敏感配置信息。

```bash
# 复制配置模板
cp .env.template .env

# 编辑 .env 文件,填写你的真实配置
# Windows: notepad .env
# Linux/Mac: vim .env
```

需要配置的项:
- **腾讯云 COS**: SecretId、SecretKey、Bucket、Region
- **数据库**: URL、用户名、密码
- **Redis**: 主机、端口、密码(如果有)

### 3. 初始化数据库

执行 `sql/create_table.sql` 创建数据表。

### 4. 启动项目

#### 方式一: 使用 Maven

```bash
mvn spring-boot:run
```

#### 方式二: 打包后运行

```bash
mvn clean package -DskipTests
java -jar target/yu-picture-backend-0.0.1-SNAPSHOT.jar
```

#### 方式三: 使用 Docker Compose (推荐)

```bash
docker-compose up -d
```

### 5. 访问应用

- **API 接口文档**: http://localhost:8123/api/doc.html
- **健康检查**: http://localhost:8123/api/health

## 📁 项目结构

```
yu-picture-backend/
├── src/main/java/com/example/yupicturebackend/
│   ├── controller/          # 控制器层
│   ├── service/             # 服务层
│   ├── mapper/              # 数据访问层
│   ├── model/               # 数据模型
│   │   ├── entity/          # 实体类
│   │   ├── dto/             # 数据传输对象
│   │   ├── vo/              # 视图对象
│   │   └── enums/           # 枚举类
│   ├── config/              # 配置类
│   ├── common/              # 通用类
│   ├── exception/           # 异常处理
│   ├── aop/                 # 切面编程
│   └── manager/             # 管理器
├── src/main/resources/
│   ├── application.yml      # 主配置文件
│   ├── application-prod.yml # 生产环境配置
│   └── mapper/              # MyBatis XML
├── sql/                     # SQL 脚本
├── .env.template            # 环境变量模板
└── docker-compose.yml       # Docker 编排
```

## 🔐 安全说明

### 敏感信息管理

本项目**不包含**任何敏感的配置文件。使用前请:

1. ✅ 复制 `.env.template` 为 `.env`
2. ✅ 填写你的真实配置信息
3. ❌ **切勿**将 `.env` 文件提交到 Git

`.env` 文件已在 `.gitignore` 中配置,不会被意外提交。

### 密钥安全建议

- 定期轮换 API 密钥
- 使用最小权限原则配置云服务
- 不同环境使用不同的密钥
- 不要在任何地方硬编码密钥

## 📖 API 文档

项目集成了 Knife4j,启动后可访问:
- **Swagger UI**: http://localhost:8123/api/doc.html

主要接口:
- 用户注册/登录
- 图片上传/查询/删除
- 空间管理
- 文件管理

## 🐳 Docker 部署

### 使用 Docker Compose

```bash
# 启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

### 单独构建镜像

```bash
docker build -t yu-picture-backend .
docker run -p 8123:8123 --env-file .env yu-picture-backend
```

详细部署说明请参考 [DEPLOYMENT.md](DEPLOYMENT.md)

## 🧪 测试

```bash
# 运行单元测试
mvn test

# 跳过测试打包
mvn clean package -DskipTests
```

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request!

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📄 开源协议

本项目采用 [MIT License](LICENSE) 开源协议。

## ⚠️ 免责声明

本项目仅供学习交流使用,请勿用于商业用途。使用过程中造成的任何问题由使用者自行承担。

## 📮 联系方式

如有问题,请提交 Issue 或通过以下方式联系:
- Email: your-email@example.com
- GitHub Issues: https://github.com/your-username/yu-picture-backend/issues

---

**Star ⭐ 如果这个项目对你有帮助!**
