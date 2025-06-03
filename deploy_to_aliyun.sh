#!/bin/bash

# 阿里云服务器一键部署脚本
# 作者: AI Assistant
# 使用方法: chmod +x deploy_to_aliyun.sh && ./deploy_to_aliyun.sh

set -e

echo "========================================"
echo "Sky Takeout 阿里云服务器一键部署"
echo "========================================"

# 配置变量
SERVER_IP="47.88.35.159"  # 用户的阿里云服务器IP
SERVER_USER="root"
PROJECT_DIR="/opt/sky-takeout"
DB_NAME="happy_tassie_travel"
DB_USER="skyapp"
DB_PASSWORD="Sky2024@Strong!"

# 检查参数
if [ $# -eq 0 ]; then
    echo "请输入服务器IP地址:"
    read SERVER_IP
else
    SERVER_IP=$1
fi

if [ -z "$SERVER_IP" ]; then
    echo "错误: 服务器IP地址不能为空"
    exit 1
fi

echo "开始部署到服务器: $SERVER_IP"

# 1. 本地准备 - 打包项目
echo "========================================"
echo "步骤 1: 本地打包项目"
echo "========================================"

echo "清理并编译项目..."
mvn clean package -DskipTests

if [ ! -f "sky-server/target/sky-server-1.0-SNAPSHOT.jar" ]; then
    echo "错误: JAR文件构建失败"
    exit 1
fi

echo "项目打包完成"

# 2. 上传文件到服务器
echo "========================================"
echo "步骤 2: 上传文件到服务器"
echo "========================================"

echo "创建服务器目录..."
ssh $SERVER_USER@$SERVER_IP "mkdir -p $PROJECT_DIR"

echo "上传项目文件..."
scp -r ./* $SERVER_USER@$SERVER_IP:$PROJECT_DIR/

echo "文件上传完成"

# 3. 在服务器上执行安装脚本
echo "========================================"
echo "步骤 3: 服务器环境安装"
echo "========================================"

echo "在服务器上安装基础环境..."
ssh $SERVER_USER@$SERVER_IP << 'ENDSSH'
cd /opt/sky-takeout
chmod +x quick_deploy.sh
./quick_deploy.sh
ENDSSH

echo "基础环境安装完成"

# 4. 配置数据库
echo "========================================"
echo "步骤 4: 配置数据库"
echo "========================================"

echo "配置MySQL数据库..."
ssh $SERVER_USER@$SERVER_IP << ENDSSH
cd $PROJECT_DIR

# MySQL安全配置
mysql_secure_installation

# 创建数据库和用户
chmod +x deploy_database.sh
./deploy_database.sh
ENDSSH

echo "数据库配置完成"

# 5. 安装Redis
echo "========================================"
echo "步骤 5: 安装Redis"
echo "========================================"

ssh $SERVER_USER@$SERVER_IP << 'ENDSSH'
# 检测操作系统并安装Redis
if [ -f /etc/redhat-release ]; then
    yum install -y redis
else
    apt install -y redis-server
fi

# 启动Redis
systemctl start redis
systemctl enable redis

# 验证Redis
redis-cli ping
ENDSSH

echo "Redis安装完成"

# 6. 部署应用
echo "========================================"
echo "步骤 6: 部署应用"
echo "========================================"

ssh $SERVER_USER@$SERVER_IP << ENDSSH
cd $PROJECT_DIR

# 启动应用服务
systemctl start sky-takeout
systemctl enable sky-takeout

# 等待服务启动
sleep 10

# 检查服务状态
systemctl status sky-takeout

# 检查应用健康状态
curl -f http://localhost:8080/doc.html || echo "应用可能还在启动中..."
ENDSSH

echo "应用部署完成"

# 7. 配置防火墙和安全组
echo "========================================"
echo "步骤 7: 配置网络访问"
echo "========================================"

ssh $SERVER_USER@$SERVER_IP << 'ENDSSH'
# 配置防火墙规则
if command -v firewall-cmd >/dev/null 2>&1; then
    # CentOS/RHEL
    firewall-cmd --permanent --add-port=8080/tcp
    firewall-cmd --permanent --add-port=80/tcp
    firewall-cmd --reload
elif command -v ufw >/dev/null 2>&1; then
    # Ubuntu/Debian
    ufw allow 8080/tcp
    ufw allow 80/tcp
    echo "y" | ufw enable
fi

echo "防火墙配置完成"
echo "请记得在阿里云控制台的安全组中开放8080端口"
ENDSSH

# 8. 部署完成总结
echo "========================================"
echo "部署完成！"
echo "========================================"
echo ""
echo "服务器信息:"
echo "  - IP地址: $SERVER_IP"
echo "  - 应用端口: 8080"
echo "  - 访问地址: http://$SERVER_IP:8080"
echo "  - API文档: http://$SERVER_IP:8080/doc.html"
echo ""
echo "数据库信息:"
echo "  - 数据库名: $DB_NAME"
echo "  - 用户名: $DB_USER"
echo "  - 密码: $DB_PASSWORD"
echo ""
echo "重要提醒:"
echo "1. 请在阿里云控制台安全组中开放8080端口"
echo "2. 建议修改默认数据库密码"
echo "3. 可以通过以下命令查看应用日志:"
echo "   ssh $SERVER_USER@$SERVER_IP 'journalctl -u sky-takeout -f'"
echo ""
echo "如果遇到问题，请检查:"
echo "1. 服务状态: systemctl status sky-takeout"
echo "2. 应用日志: journalctl -u sky-takeout"
echo "3. 数据库连接: mysql -u $DB_USER -p"
echo ""
echo "部署脚本执行完成！"
echo "========================================" 