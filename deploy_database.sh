#!/bin/bash

# 数据库部署脚本
# 使用方法: ./deploy_database.sh

set -e

echo "========================================"
echo "开始部署数据库"
echo "========================================"

# 数据库配置
DB_NAME="happy_tassie_travel"
DB_USER="skyapp"
DB_PASSWORD="Sky2024@Strong!"  # 与其他脚本保持一致

echo "请输入MySQL root密码:"
read -s ROOT_PASSWORD

# 1. 创建数据库和用户
echo "创建数据库和用户..."
mysql -u root -p$ROOT_PASSWORD << EOF
-- 创建数据库
CREATE DATABASE IF NOT EXISTS $DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户（如果不存在）
CREATE USER IF NOT EXISTS '$DB_USER'@'%' IDENTIFIED BY '$DB_PASSWORD';
CREATE USER IF NOT EXISTS '$DB_USER'@'localhost' IDENTIFIED BY '$DB_PASSWORD';

-- 授权
GRANT ALL PRIVILEGES ON $DB_NAME.* TO '$DB_USER'@'%';
GRANT ALL PRIVILEGES ON $DB_NAME.* TO '$DB_USER'@'localhost';

-- 刷新权限
FLUSH PRIVILEGES;

-- 显示创建的用户
SELECT user, host FROM mysql.user WHERE user = '$DB_USER';
EOF

# 2. 导入数据库结构
echo "导入数据库结构..."
if [ -f "sky-server/src/main/resources/sql/create_tables.sql" ]; then
    mysql -u $DB_USER -p$DB_PASSWORD $DB_NAME < sky-server/src/main/resources/sql/create_tables.sql
    echo "基础表结构导入完成"
fi

if [ -f "sky-server/src/main/resources/sql/alter_tables.sql" ]; then
    mysql -u $DB_USER -p$DB_PASSWORD $DB_NAME < sky-server/src/main/resources/sql/alter_tables.sql
    echo "表结构更新完成"
fi

if [ -f "sky-server/src/main/resources/sql/chatbot.sql" ]; then
    mysql -u $DB_USER -p$DB_PASSWORD $DB_NAME < sky-server/src/main/resources/sql/chatbot.sql
    echo "聊天机器人表结构导入完成"
fi

# 3. 导入测试数据（如果存在）
if [ -f "test_data.sql" ]; then
    echo "导入测试数据..."
    mysql -u $DB_USER -p$DB_PASSWORD $DB_NAME < test_data.sql
    echo "测试数据导入完成"
fi

# 4. 验证数据库
echo "验证数据库连接..."
mysql -u $DB_USER -p$DB_PASSWORD $DB_NAME -e "SHOW TABLES;"

echo "========================================"
echo "数据库部署完成！"
echo "========================================"
echo "数据库名: $DB_NAME"
echo "用户名: $DB_USER"
echo "密码: $DB_PASSWORD"
echo "========================================" 