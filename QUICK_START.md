# 🚀 快速启动指南

## ⚡ 第一次运行项目

### 1️⃣ 配置敏感信息

```bash
# 1. 查看备份的敏感信息
cat SECRETS_BACKUP.txt

# 2. 在 IntelliJ IDEA 中配置环境变量
# Run > Edit Configurations > Environment variables
# 从 SECRETS_BACKUP.txt 复制对应的值
```

### 2️⃣ 启动数据库和Redis

```bash
# 启动MySQL
mysql.server start  # macOS
# 或
net start MySQL  # Windows

# 启动Redis
redis-server
```

### 3️⃣ 运行应用

```bash
# 方式1：使用Maven
mvn spring-boot:run

# 方式2：IDE中直接运行
# 打开 SkyApplication.java，点击运行
```

### 4️⃣ 验证

访问：`http://localhost:8080/doc.html` 查看API文档

## 📋 环境变量最小配置（开发环境）

在IDE的运行配置中添加以下环境变量：

```properties
# 必需
GOOGLE_OAUTH_CLIENT_ID=从SECRETS_BACKUP.txt获取
GOOGLE_OAUTH_CLIENT_SECRET=从SECRETS_BACKUP.txt获取
GMAIL_USERNAME=从SECRETS_BACKUP.txt获取
GMAIL_PASSWORD=从SECRETS_BACKUP.txt获取
EMAIL_ENCRYPTION_KEY=从SECRETS_BACKUP.txt获取

# 可选（使用默认值）
DB_PASSWORD=abc123
WEATHER_API_KEY=从SECRETS_BACKUP.txt获取
```

## 📚 更多信息

- 完整配置指南：查看 `ENVIRONMENT_SETUP_GUIDE.md`
- 密钥备份：查看 `SECRETS_BACKUP.txt`（不要提交到Git）
- 部署文档：查看 `deploy/README.md`

## ⚠️ 重要提示

1. `SECRETS_BACKUP.txt` 包含真实密钥，**请妥善保管**
2. 该文件已添加到 `.gitignore`，不会被提交到Git
3. 如果丢失，需要重新在各个平台生成新的密钥

