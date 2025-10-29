# 🔧 环境变量配置指南

## 📋 概述

为了安全起见，所有敏感信息已从配置文件中移除，改用环境变量管理。敏感信息已备份到 `SECRETS_BACKUP.txt`（该文件已添加到 `.gitignore`，不会被提交到 Git）。

## 🔐 敏感信息说明

所有密钥和密码已从配置文件中移除，现在使用环境变量。请参考 `SECRETS_BACKUP.txt` 文件获取实际的配置值。

## 🚀 配置方法

### 方法 1：使用 IDE 配置（开发环境推荐）

#### IntelliJ IDEA

1. 打开 `Run/Debug Configurations`
2. 选择你的 Spring Boot 应用配置
3. 在 `Environment variables` 中添加以下变量（从 `SECRETS_BACKUP.txt` 复制真实值）：

```
GOOGLE_OAUTH_CLIENT_ID=你的Google Client ID
GOOGLE_OAUTH_CLIENT_SECRET=你的Google Client Secret
WECHAT_APPID=你的微信AppID
WECHAT_SECRET=你的微信Secret
GMAIL_USERNAME=你的Gmail邮箱
GMAIL_PASSWORD=你的Gmail应用密码
WEATHER_API_KEY=你的天气API Key
EMAIL_ENCRYPTION_KEY=你的加密密钥
DB_PASSWORD=你的数据库密码
```

4. 点击 `Apply` 和 `OK`

#### VS Code

1. 在项目根目录创建 `.vscode/launch.json`
2. 添加以下配置：

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "Spring Boot App",
      "request": "launch",
      "mainClass": "com.sky.SkyApplication",
      "env": {
        "GOOGLE_OAUTH_CLIENT_ID": "你的值",
        "GOOGLE_OAUTH_CLIENT_SECRET": "你的值",
        "WECHAT_APPID": "你的值",
        "WECHAT_SECRET": "你的值",
        "GMAIL_USERNAME": "你的值",
        "GMAIL_PASSWORD": "你的值",
        "WEATHER_API_KEY": "你的值",
        "EMAIL_ENCRYPTION_KEY": "你的值"
      }
    }
  ]
}
```

### 方法 2：使用系统环境变量

#### Windows

1. 打开 `系统属性` > `高级` > `环境变量`
2. 在 `用户变量` 中添加所有需要的变量
3. 重启 IDE 使环境变量生效

#### macOS/Linux

在 `~/.bash_profile` 或 `~/.zshrc` 中添加：

```bash
export GOOGLE_OAUTH_CLIENT_ID="你的值"
export GOOGLE_OAUTH_CLIENT_SECRET="你的值"
export WECHAT_APPID="你的值"
export WECHAT_SECRET="你的值"
export GMAIL_USERNAME="你的值"
export GMAIL_PASSWORD="你的值"
export WEATHER_API_KEY="你的值"
export EMAIL_ENCRYPTION_KEY="你的值"
```

然后执行：
```bash
source ~/.bash_profile  # 或 source ~/.zshrc
```

### 方法 3：使用 .env 文件（生产环境）

1. 在项目根目录创建 `.env` 文件（已在 `.gitignore` 中）
2. 从 `SECRETS_BACKUP.txt` 复制内容到 `.env`
3. 使用 Spring Boot 的 `spring-boot-starter-dotenv` 或类似工具加载

```env
GOOGLE_OAUTH_CLIENT_ID=你的值
GOOGLE_OAUTH_CLIENT_SECRET=你的值
WECHAT_APPID=你的值
WECHAT_SECRET=你的值
GMAIL_USERNAME=你的值
GMAIL_PASSWORD=你的值
WEATHER_API_KEY=你的值
EMAIL_ENCRYPTION_KEY=你的值
DB_PASSWORD=你的值
```

### 方法 4：Docker/Docker Compose

在 `docker-compose.yml` 中添加环境变量：

```yaml
version: '3.8'
services:
  app:
    image: sky-takeout-backend
    environment:
      - GOOGLE_OAUTH_CLIENT_ID=${GOOGLE_OAUTH_CLIENT_ID}
      - GOOGLE_OAUTH_CLIENT_SECRET=${GOOGLE_OAUTH_CLIENT_SECRET}
      - WECHAT_APPID=${WECHAT_APPID}
      - WECHAT_SECRET=${WECHAT_SECRET}
      - GMAIL_USERNAME=${GMAIL_USERNAME}
      - GMAIL_PASSWORD=${GMAIL_PASSWORD}
      - WEATHER_API_KEY=${WEATHER_API_KEY}
      - EMAIL_ENCRYPTION_KEY=${EMAIL_ENCRYPTION_KEY}
    env_file:
      - .env
```

## 📝 所需环境变量清单

### 必需变量

| 变量名 | 说明 | 示例 |
|--------|------|------|
| `GOOGLE_OAUTH_CLIENT_ID` | Google OAuth 客户端ID | `862846399763-xxx.apps.googleusercontent.com` |
| `GOOGLE_OAUTH_CLIENT_SECRET` | Google OAuth 客户端密钥 | `GOCSPX-xxxxx` |
| `GMAIL_USERNAME` | Gmail 发件邮箱 | `your-email@gmail.com` |
| `GMAIL_PASSWORD` | Gmail 应用专用密码 | `xxxx xxxx xxxx xxxx` |
| `EMAIL_ENCRYPTION_KEY` | 邮箱密码加密密钥（32字符） | `HappyTassieTravel2024EmailKey32` |

### 可选变量（有默认值）

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `WECHAT_APPID` | 微信小程序AppID | `your-wechat-appid` |
| `WECHAT_SECRET` | 微信小程序Secret | `your-wechat-secret` |
| `WEATHER_API_KEY` | OpenWeatherMap API Key | `your-weather-api-key` |
| `DB_HOST` | 数据库主机 | `localhost` |
| `DB_PORT` | 数据库端口 | `3306` |
| `DB_NAME` | 数据库名称 | `happy_tassie_travel` |
| `DB_USERNAME` | 数据库用户名 | `root` |
| `DB_PASSWORD` | 数据库密码 | `abc123` |
| `REDIS_HOST` | Redis主机 | `localhost` |
| `REDIS_PORT` | Redis端口 | `6379` |
| `REDIS_DATABASE` | Redis数据库编号 | `7` (dev) / `0` (prod) |

## ⚠️ 安全注意事项

1. **永远不要**提交 `SECRETS_BACKUP.txt` 或 `.env` 文件到Git
2. **永远不要**在公开渠道分享这些密钥
3. 定期更换密码和密钥
4. 使用不同的密钥用于开发和生产环境
5. 如果密钥泄露，立即在相应平台重新生成

## 🔍 验证配置

启动应用后，检查日志确认配置是否正确加载：

```bash
# 应该看到类似的日志
✅ Google OAuth configured with client ID: 862846399763-xxx...
✅ Gmail configured with username: Tom.zhang@htas.com.au
✅ Database connected: happy_tassie_travel
```

## 🐛 常见问题

### 1. 环境变量未生效

- 确认已重启IDE或终端
- 检查变量名是否正确（区分大小写）
- 使用 `echo $VARIABLE_NAME` (Linux/Mac) 或 `echo %VARIABLE_NAME%` (Windows) 测试

### 2. Gmail 发送邮件失败

- 确认使用的是**应用专用密码**，不是Gmail账户密码
- 在Google账户设置中启用"两步验证"
- 生成应用专用密码：https://myaccount.google.com/apppasswords

### 3. Google OAuth 回调失败

- 检查 `GOOGLE_REDIRECT_URI` 是否与Google Console中配置的一致
- 确认域名和端口号正确

## 📞 获取帮助

如有问题，请参考：
- `SECRETS_BACKUP.txt` - 密钥备份文件
- 项目文档
- 联系项目管理员

