#!/bin/bash

# ===========================================
# 塔斯马尼亚旅游系统 - 前端部署脚本
# ===========================================

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

# 配置变量
FRONTEND_DIR="/opt/frontend"
NGINX_SITE="happy-tassie-travel"

log_info "开始部署塔斯马尼亚旅游系统前端..."

# 第1步：安装依赖和构建
log_step "安装依赖并构建..."

# 检查Node.js
if ! command -v node &> /dev/null; then
    log_info "安装Node.js..."
    curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
    sudo apt-get install -y nodejs
fi

# 创建前端目录并克隆代码
sudo mkdir -p $FRONTEND_DIR
cd $FRONTEND_DIR

# 如果目录为空，克隆代码
if [ ! -f "package.json" ]; then
    log_info "克隆前端代码..."
    sudo git clone https://github.com/qhdzhm/happyUserEnd.git .
    sudo chown -R ubuntu:ubuntu $FRONTEND_DIR
fi

# 拉取最新代码
log_info "更新代码..."
git pull origin main

# 安装依赖
log_info "安装依赖..."
npm install

# 构建项目
log_info "构建项目..."
export NODE_OPTIONS="--max-old-space-size=2048"
npm run build

# 第2步：配置Nginx
log_step "配置Nginx..."

# 检查Nginx是否安装
if ! command -v nginx &> /dev/null; then
    log_info "安装Nginx..."
    sudo apt update
    sudo apt install nginx -y
fi

# 创建Nginx配置
log_info "创建Nginx站点配置..."
sudo tee /etc/nginx/sites-available/$NGINX_SITE > /dev/null <<'EOF'
server {
    listen 80;
    server_name 47.86.32.159 localhost;
    
    root /opt/frontend/build;
    index index.html index.htm;
    
    # 安全头
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    
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
    
    # 直接代理到后端的特定路径
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

# 启用站点
sudo ln -sf /etc/nginx/sites-available/$NGINX_SITE /etc/nginx/sites-enabled/

# 删除默认站点
sudo rm -f /etc/nginx/sites-enabled/default

# 测试Nginx配置
log_info "测试Nginx配置..."
sudo nginx -t

if [ $? -eq 0 ]; then
    log_info "✅ Nginx配置验证成功"
else
    log_error "❌ Nginx配置验证失败"
    exit 1
fi

# 第3步：启动服务
log_step "启动Nginx服务..."

sudo systemctl enable nginx
sudo systemctl restart nginx

if sudo systemctl is-active --quiet nginx; then
    log_info "✅ Nginx服务启动成功"
else
    log_error "❌ Nginx服务启动失败"
    sudo systemctl status nginx
    exit 1
fi

# 第4步：设置权限
log_step "设置权限..."

sudo chown -R www-data:www-data $FRONTEND_DIR/build
sudo chmod -R 755 $FRONTEND_DIR/build

# 第5步：验证部署
log_step "验证部署..."

sleep 5

# 检查端口监听
if netstat -tuln | grep -q ":80 "; then
    log_info "✅ 端口80正在监听"
else
    log_warn "⚠️  端口80未监听"
fi

# HTTP健康检查
log_info "进行HTTP健康检查..."
if curl -f -s http://localhost/ > /dev/null 2>&1; then
    log_info "✅ 本地HTTP访问正常"
else
    log_warn "⚠️  本地HTTP访问可能有问题"
fi

# 显示部署信息
echo ""
log_info "🎉 前端部署完成！"
echo ""
log_info "部署信息："
echo "  - 前端目录: $FRONTEND_DIR"
echo "  - 构建目录: $FRONTEND_DIR/build"
echo "  - Nginx配置: /etc/nginx/sites-available/$NGINX_SITE"
echo "  - 服务状态: $(sudo systemctl is-active nginx)"
echo ""
log_info "访问地址："
echo "  - 本地: http://localhost"
echo "  - 公网: http://47.86.32.159"
echo ""
log_info "查看日志："
echo "  - Nginx访问日志: sudo tail -f /var/log/nginx/access.log"
echo "  - Nginx错误日志: sudo tail -f /var/log/nginx/error.log"
echo ""
log_info "后续更新命令："
echo "  cd $FRONTEND_DIR && git pull origin main && npm run build && sudo systemctl reload nginx"
echo "" 