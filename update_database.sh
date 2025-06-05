#!/bin/bash

# ===========================================
# 塔斯马尼亚旅游系统 - 数据库更新脚本
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

# 数据库配置
DB_HOST="localhost"
DB_PORT="3306"
DB_NAME="happy_tassie_travel"
DB_USER="root"
DB_PASS="abc123"
BACKUP_DIR="/opt/backups"
SQL_DIR="/opt/sky-takeout/sky-server/src/main/resources/sql"

echo "=============================================="
echo "🗄️ 数据库更新脚本"
echo "=============================================="

# 第1步：检查MySQL连接
log_step "检查MySQL连接..."
if ! mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -e "SELECT 1;" >/dev/null 2>&1; then
    log_error "无法连接到MySQL数据库，请检查连接参数"
    exit 1
fi
log_info "MySQL连接正常"

# 第2步：创建备份目录
log_step "创建备份目录..."
mkdir -p $BACKUP_DIR
log_info "备份目录已准备: $BACKUP_DIR"

# 第3步：备份现有数据库
log_step "备份现有数据库..."
BACKUP_FILE="$BACKUP_DIR/happy_tassie_travel_backup_$(date +%Y%m%d_%H%M%S).sql"
log_info "正在备份到: $BACKUP_FILE"

if mysqldump -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS $DB_NAME > $BACKUP_FILE 2>/dev/null; then
    log_info "✅ 数据库备份成功"
else
    log_error "数据库备份失败"
    exit 1
fi

# 第4步：检查并执行SQL更新
log_step "执行数据库更新..."

# 更新导游车辆分配表
if [ -f "$SQL_DIR/tour_guide_vehicle_assignment.sql" ]; then
    log_info "更新导游车辆分配表..."
    mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS $DB_NAME < "$SQL_DIR/tour_guide_vehicle_assignment.sql"
    log_info "✅ 导游车辆分配表更新完成"
fi

# 更新聊天机器人表
if [ -f "$SQL_DIR/chatbot.sql" ]; then
    log_info "更新聊天机器人表..."
    mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS $DB_NAME < "$SQL_DIR/chatbot.sql"
    log_info "✅ 聊天机器人表更新完成"
fi

# 更新其他表
if [ -f "$SQL_DIR/create_tables.sql" ]; then
    log_info "更新其他表..."
    mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS $DB_NAME < "$SQL_DIR/create_tables.sql"
    log_info "✅ 其他表更新完成"
fi

# 第5步：验证更新
log_step "验证数据库更新..."

# 检查表是否存在
TABLES=(
    "tour_guide_vehicle_assignment"
    "chat_message"
    "chat_session"
    "chatbot_usage"
    "chatbot_blacklist"
    "vehicle_driver"
)

for table in "${TABLES[@]}"; do
    if mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS $DB_NAME -e "SHOW TABLES LIKE '$table';" | grep -q $table; then
        log_info "✅ 表 $table 存在"
    else
        log_warn "⚠️  表 $table 不存在"
    fi
done

# 第6步：显示数据库信息
log_step "数据库信息..."
echo ""
log_info "数据库统计信息:"
mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS $DB_NAME -e "
SELECT 
    TABLE_NAME as '表名',
    TABLE_ROWS as '记录数',
    ROUND(((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024), 2) as '大小(MB)'
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = '$DB_NAME'
ORDER BY TABLE_NAME;
"

echo ""
echo "=============================================="
log_info "🎉 数据库更新完成!"
echo "=============================================="
echo ""
log_info "备份文件: $BACKUP_FILE"
log_info "如有问题，可使用备份文件恢复数据库:"
echo "mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS $DB_NAME < $BACKUP_FILE" 