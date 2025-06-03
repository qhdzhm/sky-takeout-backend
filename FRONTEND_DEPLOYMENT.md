# 🌐 前端部署指南

## 📋 概述

塔斯马尼亚旅游系统前端部署指南，包含完整的构建和部署流程。

## 🔧 前置要求

- Ubuntu 22.04 服务器
- Node.js 18+
- Nginx
- Git

## 🚀 自动部署

### 方法1：使用部署脚本（推荐）

在服务器上执行：

```bash
# 1. 下载部署脚本
curl -o deploy-frontend.sh https://raw.githubusercontent.com/qhdzhm/sky-takeout-backend/main/deploy-frontend.sh

# 2. 设置执行权限
chmod +x deploy-frontend.sh

# 3. 运行部署脚本
./deploy-frontend.sh
```

### 方法2：手动部署

```bash
# 1. 安装Node.js
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt-get install -y nodejs

# 2. 创建前端目录
sudo mkdir -p /opt/frontend
cd /opt/frontend

# 3. 克隆前端代码
sudo git clone https://github.com/qhdzhm/happyUserEnd.git .
sudo chown -R ubuntu:ubuntu /opt/frontend

# 4. 安装依赖
npm install

# 5. 构建项目
export NODE_OPTIONS="--max-old-space-size=2048"
npm run build

# 6. 安装Nginx
sudo apt update
sudo apt install nginx -y

# 7. 配置Nginx
sudo tee /etc/nginx/sites-available/happy-tassie-travel > /dev/null <<'EOF'
server {
    listen 80;
    server_name 47.86.32.159 localhost;
    
    root /opt/frontend/build;
    index index.html index.htm;
    
    # React Router支持
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    # API代理到后端
    location /api/ {
        proxy_pass http://localhost:8080/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
    }
    
    # 后端API代理
    location ~ ^/(admin|user|common|agent|tour|booking|payment|chat|credit)/ {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
    }
    
    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
        access_log off;
    }
    
    # 压缩配置
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain text/css text/xml text/javascript application/javascript application/xml+rss application/json;
}
EOF

# 8. 启用站点
sudo ln -sf /etc/nginx/sites-available/happy-tassie-travel /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default

# 9. 启动Nginx
sudo nginx -t
sudo systemctl enable nginx
sudo systemctl restart nginx

# 10. 设置权限
sudo chown -R www-data:www-data /opt/frontend/build
sudo chmod -R 755 /opt/frontend/build
```

## 🔄 更新部署

每次前端代码更新后：

```bash
cd /opt/frontend
git pull origin main
npm run build
sudo systemctl reload nginx
```

## ✅ 验证部署

```bash
# 检查Nginx状态
sudo systemctl status nginx

# 检查端口监听
sudo netstat -tuln | grep :80

# 测试访问
curl -I http://localhost
curl -I http://47.86.32.159
```

## 📊 访问地址

- **前端网站**: http://47.86.32.159
- **后端API**: http://47.86.32.159:8080

## 🔍 故障排查

### 常见问题

1. **构建内存不足**
   ```bash
   export NODE_OPTIONS="--max-old-space-size=2048"
   npm run build
   ```

2. **权限问题**
   ```bash
   sudo chown -R www-data:www-data /opt/frontend/build
   sudo chmod -R 755 /opt/frontend/build
   ```

3. **Nginx配置错误**
   ```bash
   sudo nginx -t
   sudo systemctl reload nginx
   ```

### 查看日志

```bash
# Nginx访问日志
sudo tail -f /var/log/nginx/access.log

# Nginx错误日志
sudo tail -f /var/log/nginx/error.log
```

## 🎯 部署检查清单

- [ ] Node.js 18+ 已安装
- [ ] 前端代码已克隆
- [ ] 依赖已安装
- [ ] 项目构建成功
- [ ] Nginx已安装和配置
- [ ] 站点配置已启用
- [ ] 权限设置正确
- [ ] 服务正常运行
- [ ] 前端网站可访问
- [ ] API代理正常工作

## 📞 技术支持

如果部署过程中遇到问题：

1. 检查Node.js版本：`node -v`
2. 检查Nginx状态：`sudo systemctl status nginx`
3. 查看错误日志：`sudo tail -f /var/log/nginx/error.log`
4. 确认后端服务运行：`sudo systemctl status sky-takeout`

## 🔧 高级配置

### SSL证书配置（可选）

```bash
# 安装Certbot
sudo apt install certbot python3-certbot-nginx -y

# 申请SSL证书
sudo certbot --nginx -d yourdomain.com

# 自动续期
sudo crontab -e
# 添加: 0 12 * * * /usr/bin/certbot renew --quiet
```

### 性能优化

```bash
# 启用HTTP/2（需要SSL）
# 在Nginx配置中添加
listen 443 ssl http2;

# 启用Brotli压缩（可选）
sudo apt install nginx-module-brotli
``` 