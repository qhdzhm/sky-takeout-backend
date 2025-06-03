#!/bin/bash

# 阿里云服务器快速部署脚本
# 使用方法: chmod +x quick_deploy.sh && ./quick_deploy.sh

set -e  # 遇到错误立即退出

echo "========================================"
echo "开始部署 Sky Takeout 到阿里云服务器"
echo "========================================"

# 检测操作系统
if [ -f /etc/redhat-release ]; then
    OS="centos"
    PKG_MANAGER="yum"
elif [ -f /etc/lsb-release ]; then
    OS="ubuntu"
    PKG_MANAGER="apt"
else
    echo "不支持的操作系统"
    exit 1
fi

echo "检测到操作系统: $OS"

# 1. 更新系统
echo "正在更新系统..."
if [ "$OS" == "centos" ]; then
    yum update -y
else
    apt update && apt upgrade -y
fi

# 2. 安装Java
echo "正在安装Java..."
if [ "$OS" == "centos" ]; then
    yum install -y java-1.8.0-openjdk java-1.8.0-openjdk-devel
else
    apt install -y openjdk-8-jdk
fi

# 配置JAVA_HOME
echo "配置JAVA_HOME..."
if [ "$OS" == "centos" ]; then
    JAVA_HOME_PATH="/usr/lib/jvm/java-1.8.0-openjdk"
else
    JAVA_HOME_PATH="/usr/lib/jvm/java-8-openjdk-amd64"
fi

echo "export JAVA_HOME=$JAVA_HOME_PATH" >> /etc/profile
echo "export PATH=\$PATH:\$JAVA_HOME/bin" >> /etc/profile
source /etc/profile

# 3. 安装Maven
echo "正在安装Maven..."
cd /opt
wget -q https://downloads.apache.org/maven/maven-3/3.8.6/binaries/apache-maven-3.8.6-bin.tar.gz
tar -xzf apache-maven-3.8.6-bin.tar.gz
mv apache-maven-3.8.6 maven
rm apache-maven-3.8.6-bin.tar.gz

echo "export MAVEN_HOME=/opt/maven" >> /etc/profile
echo "export PATH=\$PATH:\$MAVEN_HOME/bin" >> /etc/profile
source /etc/profile

# 4. 安装MySQL
echo "正在安装MySQL..."
if [ "$OS" == "centos" ]; then
    yum install -y mysql-server
else
    apt install -y mysql-server
fi

# 启动MySQL
systemctl start mysqld
systemctl enable mysqld

# 5. 创建应用目录
echo "创建应用目录..."
mkdir -p /opt/sky-takeout
mkdir -p /opt/scripts
mkdir -p /opt/backups

# 6. 配置防火墙
echo "配置防火墙..."
if [ "$OS" == "centos" ]; then
    if systemctl is-active --quiet firewalld; then
        firewall-cmd --permanent --add-port=8080/tcp
        firewall-cmd --permanent --add-port=3306/tcp
        firewall-cmd --reload
    fi
else
    ufw allow 8080/tcp
    ufw allow 3306/tcp
    echo "y" | ufw enable
fi

# 7. 创建数据库备份脚本
echo "创建数据库备份脚本..."
cat > /opt/scripts/backup_database.sh << 'EOF'
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/opt/backups"
mkdir -p $BACKUP_DIR

# 请在这里替换为实际的数据库密码
DB_PASSWORD="Sky2024@Strong!"

mysqldump -u skyapp -p$DB_PASSWORD happy_tassie_travel > $BACKUP_DIR/happy_tassie_travel_$DATE.sql
gzip $BACKUP_DIR/happy_tassie_travel_$DATE.sql

# 删除7天前的备份
find $BACKUP_DIR -name "*.sql.gz" -mtime +7 -delete

echo "数据库备份完成: $BACKUP_DIR/happy_tassie_travel_$DATE.sql.gz"
EOF

chmod +x /opt/scripts/backup_database.sh

# 8. 创建systemd服务文件
echo "创建系统服务文件..."

# 创建日志目录
mkdir -p /opt/sky-takeout/logs

cat > /etc/systemd/system/sky-takeout.service << 'EOF'
[Unit]
Description=Sky Takeout Application
After=mysql.service
Requires=mysql.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/sky-takeout/sky-take-out
ExecStart=/usr/bin/java -Xms512m -Xmx1024m -Dspring.profiles.active=prod -jar sky-server/target/sky-server-1.0-SNAPSHOT.jar
Restart=always
RestartSec=10
Environment=JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload

echo "========================================"
echo "基础环境安装完成！"
echo "========================================"
echo ""
echo "下一步需要手动完成："
echo "1. 配置MySQL root密码: mysql_secure_installation"
echo "2. 创建数据库和用户（参考部署指南第3.3节）"
echo "3. 上传代码到 /opt/sky-takeout/"
echo "4. 修改应用配置文件中的数据库连接"
echo "5. 编译和部署应用: mvn clean package -DskipTests"
echo "6. 启动服务: systemctl start sky-takeout"
echo ""
echo "完整部署指南请查看 阿里云部署指南.md"
echo "========================================"

# 显示版本信息
echo "安装的软件版本："
java -version
mvn -version
mysql --version 