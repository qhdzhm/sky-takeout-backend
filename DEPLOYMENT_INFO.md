# 🚀 塔斯马尼亚旅游系统后端部署信息

## 📊 项目信息
- **项目名称**: 塔斯马尼亚旅游系统后端
- **版本**: 1.0-SNAPSHOT
- **构建时间**: 2024-12-21 12:00:00
- **技术栈**: Spring Boot 2.7.3 + MySQL + Redis

## 🏗️ 项目结构
```
sky-take-out/
├── sky-common/          # 通用模块
├── sky-pojo/           # 实体类模块  
├── sky-server/         # 服务器模块
├── deploy_server_update.sh    # 自动部署脚本
├── production.env      # 生产环境配置
└── SERVER_DEPLOYMENT_COMMANDS.md  # 部署文档
```

## 🔧 部署方式

### 服务器部署
1. 将代码推送到Git仓库
2. 在服务器上执行: `./deploy_server_update.sh`
3. 系统会自动完成构建和部署

### 本地开发
1. 确保MySQL数据库运行在localhost:3306
2. 数据库名: happy_tassie_travel
3. 用户名: root, 密码: abc123
4. 运行: `mvn spring-boot:run`

## 📋 数据库配置
```yaml
datasource:
  driver-class-name: com.mysql.cj.jdbc.Driver
  host: localhost
  port: 3306
  database: happy_tassie_travel
  username: root
  password: abc123
```

## 🚀 快速部署指令

### 阿里云ECS部署
```bash
# SSH连接
ssh ubuntu@47.86.32.159

# 进入项目目录
cd /opt/sky-takeout

# 拉取最新代码
git pull origin main

# 执行自动部署
./deploy_server_update.sh
```

### Git操作指令
```bash
# 添加所有文件
git add .

# 提交更改
git commit -m "更新后端项目 - 准备部署"

# 推送到远程仓库
git push origin main
```

## 📝 功能特性
- 🎯 导游车辆自动分配系统
- 🤖 AI聊天机器人 (DeepSeek集成)
- 💳 信用系统管理
- 📅 行程安排管理
- 👥 用户和代理商管理
- 🚗 车辆可用性管理
- 📊 数据分析和报表

## 📈 系统要求
- Java 8+
- Maven 3.6+
- MySQL 8.0+
- Redis (可选)

## 📞 技术支持
如需技术支持，请联系开发团队。

---
**最后更新**: 2024-12-21 