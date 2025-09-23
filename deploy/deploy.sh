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

# 如果jar包已经上传，继续部署
if [ -f "$APP_DIR/app.jar" ]; then
    echo "🚀 检测到应用文件，开始部署..."
    
    echo "🗄️  导入数据库..."
    if [ -f /tmp/database-full-backup.sql ]; then
        mysql -u skyapp -pSky2024@Strong! happy_tassie_travel < /tmp/database-full-backup.sql
        echo "✅ 完整数据库导入完成"
    elif [ -f /tmp/database-structure.sql ]; then
        mysql -u skyapp -pSky2024@Strong! happy_tassie_travel < /tmp/database-structure.sql
        echo "✅ 数据库结构导入完成"
    fi
    
    echo "📁 设置文件权限..."
    chmod +x $APP_DIR/app.jar
    chown -R www-data:www-data /var/www/user-frontend/ 2>/dev/null || true
    chown -R www-data:www-data /var/www/admin-frontend/ 2>/dev/null || true
    chmod -R 755 /var/www/user-frontend/ 2>/dev/null || true
    chmod -R 755 /var/www/admin-frontend/ 2>/dev/null || true
    
    echo "🚀 启动应用..."
    systemctl daemon-reload
    systemctl start $APP_NAME
    
    sleep 5
    
    if systemctl is-active --quiet $APP_NAME; then
        echo "✅ 应用启动成功！"
        echo "🌐 访问地址："
        echo "  用户端: http://your-domain.com/"
        echo "  管理后台: http://your-domain.com/admin/"
        echo ""
        echo "💡 申请SSL证书: certbot --nginx -d your-domain.com"
    else
        echo "❌ 应用启动失败，查看日志: journalctl -u $APP_NAME -n 20"
    fi
else
    echo "🔄 基础环境安装完成，接下来需要手动完成："
    echo "1. 上传应用jar包到 $APP_DIR/app.jar"
    echo "2. 上传前端构建文件到 /var/www/"
    echo "3. 上传数据库文件到 /tmp/"
    echo "4. 重新运行此脚本完成部署"
    echo "5. 申请SSL证书: certbot --nginx -d your-domain.com"
    echo ""
fi

echo "📝 查看服务状态: systemctl status $APP_NAME"
echo "📋 查看日志: journalctl -u $APP_NAME -f"
