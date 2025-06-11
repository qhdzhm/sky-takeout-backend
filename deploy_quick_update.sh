#!/bin/bash

# ===========================================
# 塔斯马尼亚旅游系统 - 快速更新部署脚本
# ===========================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }
log_step() { echo -e "${BLUE}[STEP]${NC} $1"; }

echo "🚀 开始部署塔斯马尼亚旅游系统到阿里云..."
echo "服务器IP: 47.86.32.159"
echo ""

# 第1步：部署后端
log_step "部署后端服务..."
if [ -f "./deploy_server_update.sh" ]; then
    chmod +x ./deploy_server_update.sh
    ./deploy_server_update.sh
    log_info "✅ 后端部署完成"
else
    log_error "未找到后端部署脚本"
    exit 1
fi

echo ""

# 第2步：部署前端
log_step "部署前端应用..."
if [ -f "./deploy-frontend.sh" ]; then
    chmod +x ./deploy-frontend.sh
    ./deploy-frontend.sh
    log_info "✅ 前端部署完成"
else
    log_error "未找到前端部署脚本"
    exit 1
fi

echo ""

# 第3步：最终验证
log_step "进行最终验证..."

sleep 10

# 检查后端服务
if curl -f -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    log_info "✅ 后端服务健康检查通过"
else
    log_warn "⚠️  后端服务检查未通过"
fi

# 检查前端服务
if curl -f -s http://localhost/ > /dev/null 2>&1; then
    log_info "✅ 前端服务健康检查通过"
else
    log_warn "⚠️  前端服务检查未通过"
fi

echo ""
echo "🎉 部署完成！"
echo ""
echo "访问地址："
echo "  🌐 前端: http://47.86.32.159"
echo "  📡 后端: http://47.86.32.159:8080"
echo ""
echo "监控命令："
echo "  - 后端状态: sudo systemctl status sky-takeout"
echo "  - 前端状态: sudo systemctl status nginx"
echo "  - 后端日志: sudo journalctl -u sky-takeout -f"
echo "  - 前端日志: sudo tail -f /var/log/nginx/access.log"
echo "" 