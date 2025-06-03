# 🚀 服务器部署指令

## 📋 部署概述

本文档包含在阿里云ECS服务器 (47.86.32.159) 上部署最新版本塔斯马尼亚旅游系统后端的详细步骤。

## 🔑 SSH连接服务器

```bash
# 连接到阿里云ECS服务器
ssh ubuntu@47.86.32.159
```

## 🎯 快速一键部署

### 方法1：使用自动部署脚本（推荐）

```bash
# 1. 进入项目目录
cd /opt/sky-takeout

# 2. 拉取最新部署脚本
git fetch origin
git checkout origin/main -- deploy_server_update.sh

# 3. 设置执行权限
chmod +x deploy_server_update.sh

# 4. 执行自动部署（包含备份、更新、构建、重启等所有步骤）
./deploy_server_update.sh
```

### 方法2：手动逐步部署

如果自动脚本遇到问题，可以使用以下手动步骤：

```bash
# 1. 停止现有服务
sudo systemctl stop sky-takeout

# 2. 备份当前版本
mkdir -p /opt/backups
cp /opt/sky-takeout/sky-server/target/sky-server-1.0-SNAPSHOT.jar \
   /opt/backups/sky-takeout-backup-$(date +%Y%m%d_%H%M%S).jar

# 3. 进入项目目录
cd /opt/sky-takeout

# 4. 拉取最新代码
git fetch origin
git reset --hard origin/main

# 5. 重新构建项目
mvn clean package -DskipTests=true

# 6. 启动服务
sudo systemctl start sky-takeout

# 7. 检查服务状态
sudo systemctl status sky-takeout
```

## 📊 部署验证

### 检查服务状态

```bash
# 检查系统服务状态
sudo systemctl status sky-takeout

# 查看实时日志
sudo journalctl -u sky-takeout -f

# 检查端口监听
netstat -tuln | grep 8080

# 检查进程
ps aux | grep sky-server
```

### HTTP健康检查

```bash
# 检查应用健康状态
curl -f http://localhost:8080/actuator/health

# 检查基本连通性
curl -f http://localhost:8080/

# 从外网访问测试
curl -f http://47.86.32.159:8080/
```

## 🔧 故障排查

### 常见问题解决

1. **服务启动失败**
```bash
# 查看详细错误日志
sudo journalctl -u sky-takeout --no-pager -l -n 50

# 检查Java版本
java -version

# 检查环境变量
cat /opt/sky-takeout/production.env
```

2. **端口被占用**
```bash
# 查看端口占用情况
sudo lsof -i :8080

# 强制结束占用进程
sudo kill -9 <PID>
```

3. **内存不足**
```bash
# 查看内存使用情况
free -h

# 查看磁盘空间
df -h
```

### 回滚到上一版本

如果新版本有问题，可以快速回滚：

```bash
# 1. 停止服务
sudo systemctl stop sky-takeout

# 2. 恢复备份
cp /opt/backups/sky-takeout-backup-YYYYMMDD_HHMMSS.jar \
   /opt/sky-takeout/sky-server/target/sky-server-1.0-SNAPSHOT.jar

# 3. 重启服务
sudo systemctl start sky-takeout
```

## 📈 性能监控

### 实时监控命令

```bash
# CPU和内存使用情况
top -p $(pgrep -f sky-server)

# JVM内存使用情况
jstat -gc $(pgrep -f sky-server) 5s

# 应用日志监控
tail -f /var/log/sky-takeout.log
```

## 🎉 部署成功验证

部署成功后，您应该能够：

1. ✅ **系统服务运行正常**
   ```bash
   sudo systemctl is-active sky-takeout
   # 输出：active
   ```

2. ✅ **端口监听正常**
   ```bash
   netstat -tuln | grep 8080
   # 输出：tcp6  0  0  :::8080  :::*  LISTEN
   ```

3. ✅ **HTTP访问正常**
   - 本地：http://localhost:8080
   - 公网：http://47.86.32.159:8080

4. ✅ **新功能验证**
   - 房间类型价格差异计算功能正常
   - 三人间价格识别正常
   - 价格更新实时响应

## 📝 部署记录

请在每次部署后记录以下信息：

- 部署时间：
- 部署版本：
- 功能更新：房间类型价格差异支持
- 部署状态：
- 备注：

---

## 📞 技术支持

如果部署过程中遇到问题，请检查：

1. **网络连接**：确保服务器能够访问GitHub
2. **磁盘空间**：确保有足够空间进行构建
3. **内存资源**：确保有足够内存运行Maven构建
4. **权限设置**：确保ubuntu用户有相应权限

**紧急回滚**：如果新版本有严重问题，请立即使用备份文件回滚到上一个稳定版本。 