#!/bin/bash

# ===========================================
# 塔斯马尼亚旅游系统 - 服务器更新部署脚本
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
PROJECT_DIR="/opt/sky-takeout"
SERVICE_NAME="sky-takeout"
JAR_NAME="sky-server-1.0-SNAPSHOT.jar"
BACKUP_DIR="/opt/backups"
ENV_FILE="/opt/sky-takeout/production.env"

log_info "开始更新塔斯马尼亚旅游系统后端服务..."

# 第1步：检查必要的目录和文件
log_step "检查环境..."

if [ ! -d "$PROJECT_DIR" ]; then
    log_error "项目目录不存在: $PROJECT_DIR"
    exit 1
fi

if [ ! -f "$ENV_FILE" ]; then
    log_error "环境配置文件不存在: $ENV_FILE"
    exit 1
fi

# 第2步：停止现有服务
log_step "停止现有服务..."

if sudo systemctl is-active --quiet $SERVICE_NAME; then
    log_info "停止 $SERVICE_NAME 服务..."
    sudo systemctl stop $SERVICE_NAME
    log_info "服务已停止"
else
    log_warn "$SERVICE_NAME 服务未运行"
fi

# 第3步：备份当前版本
log_step "备份当前版本..."

mkdir -p $BACKUP_DIR
BACKUP_FILE="$BACKUP_DIR/sky-takeout-backup-$(date +%Y%m%d_%H%M%S).tar.gz"

if [ -f "$PROJECT_DIR/sky-server/target/$JAR_NAME" ]; then
    log_info "创建备份: $BACKUP_FILE"
    tar -czf $BACKUP_FILE -C $PROJECT_DIR sky-server/target/$JAR_NAME
    log_info "备份创建成功"
else
    log_warn "未找到现有JAR文件，跳过备份"
fi

# 第4步：更新代码
log_step "更新代码..."

cd $PROJECT_DIR

# 检查是否是git仓库
if [ ! -d ".git" ]; then
    log_error "当前目录不是git仓库: $PROJECT_DIR"
    exit 1
fi

# 保存本地更改（如果有）
if ! git diff --quiet || ! git diff --cached --quiet; then
    log_warn "发现本地更改，正在暂存..."
    git stash push -m "Auto-stash before deployment $(date)"
fi

# 拉取最新代码
log_info "拉取最新代码..."
git fetch origin
git reset --hard origin/main

log_info "代码更新完成"

# 第5步：构建项目
log_step "构建项目..."

log_info "清理并构建项目..."
mvn clean package -DskipTests=true -q

if [ ! -f "sky-server/target/$JAR_NAME" ]; then
    log_error "构建失败：未找到 $JAR_NAME"
    exit 1
fi

log_info "项目构建成功"

# 第6步：验证JAR文件
log_step "验证JAR文件..."

JAR_SIZE=$(du -h "sky-server/target/$JAR_NAME" | cut -f1)
log_info "JAR文件大小: $JAR_SIZE"

# 第7步：启动服务
log_step "启动服务..."

log_info "启动 $SERVICE_NAME 服务..."
sudo systemctl start $SERVICE_NAME

# 第8步：检查服务状态
log_step "检查服务状态..."

sleep 10  # 等待服务启动

if sudo systemctl is-active --quiet $SERVICE_NAME; then
    log_info "✅ $SERVICE_NAME 服务启动成功"
    
    # 显示服务状态
    echo ""
    log_info "服务状态："
    sudo systemctl status $SERVICE_NAME --no-pager -l
    
    echo ""
    log_info "最新日志："
    sudo journalctl -u $SERVICE_NAME --no-pager -l -n 20
    
else
    log_error "❌ $SERVICE_NAME 服务启动失败"
    
    echo ""
    log_error "错误日志："
    sudo journalctl -u $SERVICE_NAME --no-pager -l -n 50
    
    # 尝试恢复备份
    if [ -f "$BACKUP_FILE" ]; then
        log_warn "尝试恢复备份..."
        tar -xzf $BACKUP_FILE -C $PROJECT_DIR
        sudo systemctl start $SERVICE_NAME
        
        if sudo systemctl is-active --quiet $SERVICE_NAME; then
            log_info "✅ 备份恢复成功，服务已启动"
        else
            log_error "❌ 备份恢复失败"
        fi
    fi
    
    exit 1
fi

# 第9步：健康检查
log_step "进行健康检查..."

log_info "等待应用完全启动..."
sleep 30

# 检查应用端口
if netstat -tuln | grep -q ":8080 "; then
    log_info "✅ 应用端口8080正在监听"
else
    log_warn "⚠️  应用端口8080未监听，可能仍在启动中"
fi

# 尝试健康检查HTTP请求
log_info "进行HTTP健康检查..."
if curl -f -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    log_info "✅ HTTP健康检查通过"
elif curl -f -s http://localhost:8080/ > /dev/null 2>&1; then
    log_info "✅ 应用根路径响应正常"
else
    log_warn "⚠️  HTTP健康检查未通过，应用可能仍在启动中"
fi

# 第10步：清理
log_step "清理..."

# 保留最近5个备份文件
if [ -d "$BACKUP_DIR" ]; then
    log_info "清理旧备份文件（保留最新5个）..."
    cd $BACKUP_DIR
    ls -t sky-takeout-backup-*.tar.gz | tail -n +6 | xargs -r rm -f
    log_info "清理完成"
fi

# 部署完成
echo ""
log_info "🎉 部署完成！"
echo ""
log_info "部署信息："
echo "  - 项目目录: $PROJECT_DIR"
echo "  - JAR文件: $JAR_NAME ($JAR_SIZE)"
echo "  - 备份文件: $BACKUP_FILE"
echo "  - 服务状态: $(sudo systemctl is-active $SERVICE_NAME)"
echo ""
log_info "您可以通过以下命令查看服务状态："
echo "  sudo systemctl status $SERVICE_NAME"
echo "  sudo journalctl -u $SERVICE_NAME -f"
echo ""
log_info "应用访问地址："
echo "  - 本地: http://localhost:8080"
echo "  - 公网: http://47.86.32.159:8080"
echo "" 