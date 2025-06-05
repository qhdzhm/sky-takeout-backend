#!/bin/bash

# ===========================================
# 塔斯马尼亚旅游系统 - Git提交准备脚本
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

echo "=============================================="
echo "🚀 塔斯马尼亚旅游系统 - Git提交准备"
echo "=============================================="

# 第1步：清理构建目录
log_step "清理构建目录..."
if [ -d "target" ]; then
    log_info "删除根目录target文件夹"
    rm -rf target
fi

if [ -d "sky-server/target" ]; then
    log_info "删除sky-server/target文件夹"
    rm -rf sky-server/target
fi

if [ -d "sky-common/target" ]; then
    log_info "删除sky-common/target文件夹"
    rm -rf sky-common/target
fi

if [ -d "sky-pojo/target" ]; then
    log_info "删除sky-pojo/target文件夹"
    rm -rf sky-pojo/target
fi

# 第2步：清理日志文件
log_step "清理日志文件..."
find . -name "*.log" -type f -exec rm -f {} \;
find . -name "logs" -type d -exec rm -rf {} \; 2>/dev/null || true

# 第3步：清理临时文件
log_step "清理临时文件..."
find . -name "*.tmp" -type f -exec rm -f {} \;
find . -name "*.temp" -type f -exec rm -f {} \;
find . -name "*.bak" -type f -exec rm -f {} \;
find . -name "*.backup" -type f -exec rm -f {} \;
find . -name ".DS_Store" -type f -exec rm -f {} \;

# 第4步：检查重要文件是否存在
log_step "检查重要文件..."

required_files=(
    "pom.xml"
    "sky-server/pom.xml" 
    "sky-common/pom.xml"
    "sky-pojo/pom.xml"
    "sky-server/src/main/java/com/sky/SkyApplication.java"
    "deploy_server_update.sh"
    "production.env"
    "SERVER_DEPLOYMENT_COMMANDS.md"
)

for file in "${required_files[@]}"; do
    if [ -f "$file" ]; then
        log_info "✅ $file"
    else
        log_error "❌ 缺少重要文件: $file"
    fi
done

# 第5步：检查Git状态
log_step "检查Git状态..."

if [ ! -d ".git" ]; then
    log_error "当前目录不是Git仓库"
    exit 1
fi

# 显示当前分支
current_branch=$(git branch --show-current)
log_info "当前分支: $current_branch"

# 显示未跟踪的文件
untracked_files=$(git ls-files --others --exclude-standard)
if [ -n "$untracked_files" ]; then
    log_info "未跟踪的文件:"
    echo "$untracked_files" | while read -r file; do
        echo "  - $file"
    done
fi

# 显示已修改的文件
modified_files=$(git diff --name-only)
if [ -n "$modified_files" ]; then
    log_info "已修改的文件:"
    echo "$modified_files" | while read -r file; do
        echo "  - $file"
    done
fi

# 第6步：检查敏感信息
log_step "检查敏感信息..."

sensitive_patterns=(
    "password.*="
    "secret.*="
    "api.*key.*="
    "token.*="
    "sk-[a-zA-Z0-9]+"
)

log_info "扫描敏感信息模式..."
for pattern in "${sensitive_patterns[@]}"; do
    if grep -r -i "$pattern" --include="*.java" --include="*.properties" --include="*.yml" --include="*.yaml" src/ 2>/dev/null; then
        log_warn "⚠️  发现可能的敏感信息模式: $pattern"
    fi
done

# 第7步：提供Git操作建议
log_step "Git操作建议..."

echo ""
log_info "接下来的操作建议:"
echo "1. 添加所有文件到暂存区:"
echo "   git add ."
echo ""
echo "2. 提交更改:"
echo "   git commit -m \"更新后端项目 - $(date '+%Y-%m-%d %H:%M:%S')\""
echo ""
echo "3. 推送到远程仓库:"
echo "   git push origin $current_branch"
echo ""

# 第8步：创建部署信息文件
log_step "创建部署信息..."

cat > DEPLOYMENT_INFO.md << EOF
# 🚀 部署信息

## 📊 项目信息
- **项目名称**: 塔斯马尼亚旅游系统后端
- **版本**: 1.0-SNAPSHOT
- **构建时间**: $(date '+%Y-%m-%d %H:%M:%S')
- **Java版本**: $(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
- **Maven版本**: $(mvn -version 2>&1 | head -n 1 | cut -d' ' -f3)

## 🏗️ 项目结构
\`\`\`
sky-take-out/
├── sky-common/          # 通用模块
├── sky-pojo/           # 实体类模块  
├── sky-server/         # 服务器模块
├── deploy_server_update.sh    # 自动部署脚本
├── production.env      # 生产环境配置
└── SERVER_DEPLOYMENT_COMMANDS.md  # 部署文档
\`\`\`

## 🔧 部署方式

### 服务器部署
1. 将代码推送到Git仓库
2. 在服务器上执行: \`./deploy_server_update.sh\`
3. 系统会自动完成构建和部署

### 本地开发
1. 确保MySQL数据库运行在localhost:3306
2. 数据库名: happy_tassie_travel
3. 用户名: root, 密码: abc123
4. 运行: \`mvn spring-boot:run\`

## 📋 数据库配置
\`\`\`yaml
datasource:
  driver-class-name: com.mysql.cj.jdbc.Driver
  host: localhost
  port: 3306
  database: happy_tassie_travel
  username: root
  password: abc123
\`\`\`

## 🚀 快速部署指令

### 阿里云ECS部署
\`\`\`bash
# SSH连接
ssh ubuntu@47.86.32.159

# 进入项目目录
cd /opt/sky-takeout

# 执行自动部署
./deploy_server_update.sh
\`\`\`

## 📝 更新日志
- $(date '+%Y-%m-%d'): 项目结构优化，准备Git部署
EOF

log_info "✅ 部署信息文件已创建: DEPLOYMENT_INFO.md"

echo ""
echo "=============================================="
log_info "🎉 Git提交准备完成!"
echo "=============================================="
echo ""
log_info "项目已准备好进行Git提交和部署。"
log_info "请按照上述建议执行Git操作。" 