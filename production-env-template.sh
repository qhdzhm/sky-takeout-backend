#!/bin/bash
# 生产环境变量配置模板
# 请根据实际情况填写相应的值，然后在服务器上执行: source production-env.sh

# ===========================================
# 数据库相关 (已在配置文件中直接配置)
# ===========================================
# 数据库用户: skyapp
# 数据库密码: Sky2024@Strong!

# ===========================================
# 阿里云OSS配置 (可选 - 如需文件上传功能)
# ===========================================
export SKY_ALIOSS_ENDPOINT="your-oss-endpoint"
export SKY_ALIOSS_BUCKET="your-bucket-name"
export SKY_ALIOSS_ACCESS_KEY="your-access-key"
export SKY_ALIOSS_ACCESS_SECRET="your-access-secret"

# ===========================================
# 微信小程序配置 (可选 - 如需微信登录功能)
# ===========================================
export SKY_WECHAT_APPID="your-wechat-appid"
export SKY_WECHAT_SECRET="your-wechat-secret"

# ===========================================
# JWT密钥配置 (建议修改默认值)
# ===========================================
export SKY_JWT_ADMIN_SECRET="your-strong-admin-secret-key"
export SKY_JWT_USER_SECRET="your-strong-user-secret-key"
export SKY_JWT_AGENT_SECRET="your-strong-agent-secret-key"

# ===========================================
# OpenAI配置 (可选 - 如需AI聊天功能)
# ===========================================
export OPENAI_API_KEY="your-openai-api-key"
export OPENAI_MODEL="gpt-3.5-turbo"
export OPENAI_TIMEOUT="30000"
export OPENAI_MAX_TOKENS="150"
export OPENAI_TEMPERATURE="0.7"

# ===========================================
# 聊天机器人限制配置
# ===========================================
export CHATBOT_RATE_LIMIT="10"
export CHATBOT_DAILY_LIMIT="100"
export CHATBOT_MAX_LENGTH="1000"
export CHATBOT_HISTORY_SIZE="5"

# ===========================================
# 天气API配置 (可选)
# ===========================================
export WEATHER_API_KEY="your-weather-api-key"
export WEATHER_BASE_URL="http://api.openweathermap.org/data/2.5"
export WEATHER_API_ENABLED="false"  # 生产环境默认禁用
export WEATHER_CACHE_DURATION="600"

# ===========================================
# 航班API配置 (可选)
# ===========================================
export FLIGHT_API_KEY="your-flight-api-key"
export FLIGHT_BASE_URL="http://api.aviationstack.com/v1"
export FLIGHT_API_ENABLED="false"  # 生产环境默认禁用

# ===========================================
# 文件上传配置
# ===========================================
export UPLOAD_MAX_FILE_SIZE="10MB"
export UPLOAD_MAX_REQUEST_SIZE="10MB"

# ===========================================
# 积分推荐配置
# ===========================================
export CREDIT_DIRECT_RATE="0.05"
export CREDIT_INDIRECT_RATE="0.02"

# ===========================================
# 旅游知识服务配置
# ===========================================
export TOUR_KNOWLEDGE_CACHE_ENABLED="true"
export TOUR_KNOWLEDGE_CACHE_TTL="3600"
export TOUR_KNOWLEDGE_MAX_RESULTS="8"
export TOUR_KNOWLEDGE_FALLBACK="true"
export TOUR_KNOWLEDGE_MIN_KEYWORDS="1"
export TOUR_KNOWLEDGE_SIMILARITY="0.6"

echo "环境变量配置完成！"
echo "注意：请根据实际需求填写相应的配置值"
echo "重要：请妥善保管包含敏感信息的配置文件！" 