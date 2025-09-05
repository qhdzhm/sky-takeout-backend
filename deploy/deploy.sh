#!/bin/bash

# Happy Tassie Travel - 部署脚本
# 作者：部署助手
# 日期：$(date)

echo "🚀 开始部署 Happy Tassie Travel 系统..."

# 配置变量
APP_NAME="happy-tassie-travel"
APP_DIR="/opt/$APP_NAME"
NGINX_SITES_DIR="/etc/nginx/sites-available"
NGINX_ENABLED_DIR="/etc/nginx/sites-enabled"

# 检查是否为root用户
if [[ $EUID -ne 0 ]]; then
   echo "❌ 此脚本需要root权限运行"
   exit 1
fi

echo "📦 步骤1: 更新系统包..."
apt update && apt upgrade -y

echo "☕ 步骤2: 安装Java 17..."
apt install -y openjdk-17-jdk

echo "🍃 步骤3: 安装MySQL..."
apt install -y mysql-server
systemctl start mysql
systemctl enable mysql

echo "🔴 步骤4: 安装Redis..."
apt install -y redis-server
systemctl start redis
systemctl enable redis

echo "🌐 步骤5: 安装Nginx..."
apt install -y nginx
systemctl start nginx
systemctl enable nginx

echo "📁 步骤6: 创建应用目录..."
mkdir -p $APP_DIR
mkdir -p $APP_DIR/logs
mkdir -p /var/www/user-frontend
mkdir -p /var/www/admin-frontend

echo "🔧 步骤7: 配置MySQL数据库..."
mysql -u root <<EOF
CREATE DATABASE IF NOT EXISTS happy_tassie_travel;
CREATE USER IF NOT EXISTS 'skyapp'@'localhost' IDENTIFIED BY 'Sky2024@Strong!';
GRANT ALL PRIVILEGES ON happy_tassie_travel.* TO 'skyapp'@'localhost';
FLUSH PRIVILEGES;
EOF

echo "⚙️  步骤8: 配置Nginx..."
cp /tmp/nginx.conf $NGINX_SITES_DIR/$APP_NAME
ln -sf $NGINX_SITES_DIR/$APP_NAME $NGINX_ENABLED_DIR/
rm -f $NGINX_ENABLED_DIR/default

echo "🔒 步骤9: 安装SSL证书 (Let's Encrypt)..."
apt install -y certbot python3-certbot-nginx

echo "🎯 步骤10: 创建systemd服务..."
cat > /etc/systemd/system/$APP_NAME.service <<EOF
[Unit]
Description=Happy Tassie Travel Spring Boot Application
After=syslog.target mysql.service redis.service

[Service]
User=root
ExecStart=/usr/bin/java -jar $APP_DIR/app.jar
SuccessExitStatus=143
StandardOutput=journal
StandardError=journal
SyslogIdentifier=$APP_NAME
KillMode=process
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable $APP_NAME

echo "✅ 基础环境安装完成！"
echo ""
echo "🔄 接下来需要手动完成："
echo "1. 上传应用jar包到 $APP_DIR/app.jar"
echo "2. 上传前端构建文件到 /var/www/"
echo "3. 配置环境变量"
echo "4. 导入数据库结构"
echo "5. 申请SSL证书: certbot --nginx -d your-domain.com"
echo "6. 启动服务: systemctl start $APP_NAME"
echo ""
echo "📝 查看服务状态: systemctl status $APP_NAME"
echo "📋 查看日志: journalctl -u $APP_NAME -f"
