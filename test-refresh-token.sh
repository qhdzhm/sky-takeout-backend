#!/bin/bash

# Refresh Token 功能测试脚本
# 使用方法: ./test-refresh-token.sh [服务器地址]

SERVER_URL=${1:-"http://localhost:8080"}
COOKIE_FILE="cookies.txt"

echo "=== Refresh Token 功能测试 ==="
echo "服务器地址: $SERVER_URL"
echo ""

# 清理之前的cookie文件
rm -f $COOKIE_FILE

echo "1. 测试用户登录..."
LOGIN_RESPONSE=$(curl -s -c $COOKIE_FILE -X POST \
  "$SERVER_URL/user/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }')

echo "登录响应: $LOGIN_RESPONSE"

# 检查登录是否成功
if echo "$LOGIN_RESPONSE" | grep -q '"code":1'; then
    echo "✓ 登录成功"
else
    echo "✗ 登录失败"
    exit 1
fi

echo ""
echo "2. 检查设置的Cookie..."
if [ -f $COOKIE_FILE ]; then
    echo "Cookie文件内容:"
    cat $COOKIE_FILE
    echo ""
else
    echo "✗ Cookie文件未生成"
    exit 1
fi

echo "3. 测试Token刷新..."
REFRESH_RESPONSE=$(curl -s -b $COOKIE_FILE -c $COOKIE_FILE -X POST \
  "$SERVER_URL/api/auth/refresh" \
  -H "Content-Type: application/json")

echo "刷新响应: $REFRESH_RESPONSE"

# 检查刷新是否成功
if echo "$REFRESH_RESPONSE" | grep -q '"code":1'; then
    echo "✓ Token刷新成功"
else
    echo "✗ Token刷新失败"
fi

echo ""
echo "4. 测试刷新后的API访问..."
PROFILE_RESPONSE=$(curl -s -b $COOKIE_FILE -X GET \
  "$SERVER_URL/user/profile" \
  -H "Content-Type: application/json")

echo "个人信息响应: $PROFILE_RESPONSE"

if echo "$PROFILE_RESPONSE" | grep -q '"code":1'; then
    echo "✓ 使用刷新后的Token访问API成功"
else
    echo "✗ 使用刷新后的Token访问API失败"
fi

echo ""
echo "5. 测试登出..."
LOGOUT_RESPONSE=$(curl -s -b $COOKIE_FILE -c $COOKIE_FILE -X POST \
  "$SERVER_URL/api/auth/logout" \
  -H "Content-Type: application/json")

echo "登出响应: $LOGOUT_RESPONSE"

if echo "$LOGOUT_RESPONSE" | grep -q '"code":1'; then
    echo "✓ 登出成功"
else
    echo "✗ 登出失败"
fi

echo ""
echo "6. 验证登出后Cookie清理..."
echo "登出后Cookie文件内容:"
cat $COOKIE_FILE

echo ""
echo "7. 测试登出后的Token刷新（应该失败）..."
REFRESH_AFTER_LOGOUT=$(curl -s -b $COOKIE_FILE -X POST \
  "$SERVER_URL/api/auth/refresh" \
  -H "Content-Type: application/json")

echo "登出后刷新响应: $REFRESH_AFTER_LOGOUT"

if echo "$REFRESH_AFTER_LOGOUT" | grep -q '"code":0'; then
    echo "✓ 登出后Token刷新正确失败"
else
    echo "✗ 登出后Token刷新应该失败但没有失败"
fi

# 清理
rm -f $COOKIE_FILE

echo ""
echo "=== 测试完成 ===" 
 
 
 
 