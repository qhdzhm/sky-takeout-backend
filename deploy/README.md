# Happy Tassie Travel - 生产环境部署指南

## 🚀 快速部署

### 1. 服务器要求
- **系统**: Ubuntu 20.04 LTS 或更高
- **配置**: 4核16G内存 (推荐)
- **存储**: 50GB系统盘
- **网络**: 5Mbps带宽

### 2. 域名和DNS配置
```bash
# A记录配置 (在域名提供商处设置)
your-domain.com        A    YOUR_SERVER_IP
www.your-domain.com    A    YOUR_SERVER_IP
```

### 3. 一键部署脚本
```bash
# 上传deploy.sh到服务器
scp deploy.sh root@your-server:/tmp/

# 执行部署
ssh root@your-server
chmod +x /tmp/deploy.sh
/tmp/deploy.sh
```

### 4. 应用部署
```bash
# 上传jar包
scp sky-server/target/*.jar root@your-server:/opt/happy-tassie-travel/app.jar

# 上传前端文件  
scp -r user-frontend/build/* root@your-server:/var/www/user-frontend/
scp -r admin-frontend/build/* root@your-server:/var/www/admin-frontend/

# 配置环境变量
cp production.env.template /opt/happy-tassie-travel/.env
# 编辑 .env 文件填入真实配置

# 导入数据库
mysql -u skyapp -p happy_tassie_travel < 1.sql

# 启动服务
systemctl start happy-tassie-travel
```

### 5. SSL证书申请
```bash
certbot --nginx -d your-domain.com -d www.your-domain.com
```

### 6. 常用运维命令
```bash
# 查看服务状态
systemctl status happy-tassie-travel

# 查看日志
journalctl -u happy-tassie-travel -f

# 重启服务
systemctl restart happy-tassie-travel

# 重载Nginx配置
nginx -t && nginx -s reload
```

## 📋 部署检查清单

- [ ] 服务器基础环境 (Java17, MySQL, Redis, Nginx)
- [ ] 域名DNS解析配置
- [ ] 数据库创建和用户权限
- [ ] 应用jar包部署
- [ ] 前端文件部署
- [ ] 环境变量配置
- [ ] 数据库结构导入
- [ ] SSL证书配置
- [ ] 服务启动和测试
- [ ] 防火墙和安全组配置

## 🔧 故障排除

### 应用无法启动
```bash
# 检查Java版本
java -version

# 检查端口占用
netstat -tlnp | grep 8080

# 查看详细日志
journalctl -u happy-tassie-travel --no-pager -l
```

### 数据库连接失败
```bash
# 测试数据库连接
mysql -u skyapp -p -h localhost happy_tassie_travel

# 检查MySQL服务
systemctl status mysql
```

### 前端页面无法访问
```bash
# 检查Nginx配置
nginx -t

# 检查文件权限
ls -la /var/www/user-frontend/

# 重启Nginx
systemctl restart nginx
```

## 📞 联系支持

如遇部署问题，请提供以下信息：
- 服务器配置和系统版本
- 错误日志截图
- 执行的具体步骤
