# 🔒 开源前安全检查清单

## ✅ 已完成的防护措施

### 1. 敏感信息清理
- [x] 移除 `.env.example` 中的真实 COS 密钥
- [x] 移除 `application.yml` 中的真实数据库密码
- [x] 移除 `application-prod.yml` 中的硬编码凭证
- [x] 创建 `.env.template` 作为配置模板

### 2. Git 忽略配置
- [x] `.gitignore` 已包含 `.env` 文件
- [x] `.gitignore` 已包含 `application-local.yml` 文件
- [x] `.gitignore` 已包含 IDE 配置文件（.idea/）
- [x] `.gitignore` 已包含编译输出目录（target/）

## ⚠️ 开源前必须检查的项目

### 1. 代码审查
- [ ] 搜索代码中是否还有其他硬编码的密码/密钥
  ```bash
  # 在 PowerShell 中执行
  grep -r "password\|secret\|key\|token" --include="*.java" --include="*.yml" --include="*.properties" src/
  ```

- [ ] 检查是否有测试文件中包含真实凭证
- [ ] 确认所有配置文件使用环境变量或占位符

### 2. 历史提交记录清理
**重要**: 即使现在删除了敏感信息,如果之前的 Git 提交中包含过,仍然可以从历史记录中找回!

**解决方案**:
```bash
# 方法1: 清除 Git 历史（推荐用于新仓库）
git checkout --orphan latest_branch
git add -A
git commit -am "Initial commit - cleaned sensitive data"
git branch -D main
git branch -m main
git push -f origin main

# 方法2: 使用 BFG Repo-Cleaner 工具清理历史
# 下载 BFG: https://rtyley.github.io/bfg-repo-cleaner/
java -jar bfg.jar --replace-text passwords.txt my-repo.git
```

### 3. 文档更新
- [ ] 创建 `README.md` 说明项目用途和快速开始
- [ ] 在 README 中添加配置说明，引导用户复制 `.env.template`
- [ ] 添加贡献指南（CONTRIBUTING.md）
- [ ] 添加许可证文件（LICENSE）

### 4. 依赖安全
- [ ] 检查 `pom.xml` 中是否有私有依赖
- [ ] 确认所有依赖都是公开可用的
- [ ] 考虑运行依赖漏洞扫描：
  ```bash
  mvn dependency-check:check
  ```

### 5. 接口和数据安全
- [ ] 确认没有暴露内部管理接口
- [ ] 检查是否有测试数据包含真实用户信息
- [ ] 确认 API 密钥、Token 等使用环境变量
- [ ] 检查 CORS 配置是否过于宽松

### 6. 日志和调试信息
- [ ] 确认生产环境关闭了详细日志
- [ ] 移除代码中的 System.out.println() 调试语句
- [ ] 检查是否有打印敏感信息的日志

## 📋 推荐的 .gitignore 增强

当前 `.gitignore` 已包含基本配置,建议确认以下文件被忽略:

```gitignore
# 敏感配置文件
.env
application-local.yml
*-local.yml

# 证书和密钥文件
*.pem
*.key
*.crt
*.p12
*.jks

# 日志文件
*.log
logs/

# 临时文件
*.tmp
*.swp
.DS_Store
```

## 🔐 最佳实践建议

### 1. 配置管理
- ✅ 使用环境变量管理敏感信息
- ✅ 提供 `.env.template` 作为示例
- ✅ 在文档中明确说明需要配置的项

### 2. 密钥轮换
如果你之前已经提交过真实密钥到 Git:
- ⚠️ **立即更换所有泄露的密钥!**
  - 腾讯云 COS SecretId/SecretKey
  - 数据库密码
  - Redis 密码
  - 任何其他 API 密钥

### 3. 访问控制
- 为不同环境使用不同的密钥
- 使用最小权限原则配置云服务
- 定期轮换密钥

### 4. 文档提示
在 README 中添加安全提示:
```markdown
## ⚠️ 安全提示

本项目不包含敏感配置信息。使用前请：
1. 复制 `.env.template` 为 `.env`
2. 填写你的真实配置信息
3. 切勿将 `.env` 文件提交到 Git
```

## 🚀 开源发布检查

- [ ] 清理 Git 历史中的敏感信息
- [ ] 更换所有可能泄露的密钥
- [ ] 完善 README 文档
- [ ] 添加 LICENSE 文件
- [ ] 检查依赖安全性
- [ ] 测试项目能否正常启动（使用示例配置）
- [ ] 移除个人电脑路径相关的配置
- [ ] 检查注释中是否有敏感信息

## 📞 如果不小心泄露了怎么办？

1. **立即撤销泄露的密钥** - 在云服务商控制台重置
2. **清理 Git 历史** - 使用 BFG 或 git filter-branch
3. **强制推送** - `git push --force`
4. **通知相关人员** - 如果有协作者
5. **监控异常使用** - 检查是否有未授权访问

---

**记住**: 一旦敏感信息提交到 Git,即使后来删除了,仍然可以从历史记录中恢复!
最好的做法是从一开始就使用环境变量管理敏感信息。
