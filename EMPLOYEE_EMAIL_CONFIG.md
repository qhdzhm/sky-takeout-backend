# 员工个人邮箱发送系统配置指南

## 📧 系统概述

员工个人邮箱发送系统允许每个员工使用自己的邮箱账号发送酒店预定邮件，而不是统一使用系统默认邮箱。这样可以提高邮件的个性化程度和送达率。

## 🚀 功能特性

- ✅ **个性化发送**: 每个员工用自己的邮箱发送，更专业
- ✅ **分散风险**: 不会因为一个邮箱被限制影响整个系统
- ✅ **责任追踪**: 可以明确知道是哪个员工发送的邮件
- ✅ **自动回退**: 如果员工邮箱发送失败，自动使用系统默认邮箱
- ✅ **安全加密**: 邮箱密码使用AES加密存储
- ✅ **连接测试**: 支持测试邮箱连接是否正常

## 📋 配置步骤

### 1. 数据库更新

首先执行SQL脚本更新数据库表结构：

```sql
-- 执行以下SQL文件
source sky-server/src/main/resources/sql/employee_email_config.sql
```

### 2. Gmail邮箱配置

对于Gmail邮箱，需要开启"应用专用密码"：

1. 登录Gmail账户
2. 进入 **账户设置** → **安全性**
3. 开启 **2步验证**（必须先开启）
4. 点击 **应用专用密码**
5. 选择应用类型（选择"邮件"）
6. 生成16位应用专用密码
7. 在系统中使用这个16位密码，而不是Gmail登录密码

### 3. 系统配置

#### 后端配置

在 `application-dev.yml` 中添加加密密钥（可选）：

```yaml
sky:
  email:
    encryption-key: "YourCustomEncryptionKey2024" # 自定义加密密钥，默认为 SkyTravelEmailKey2024
```

#### 前端配置

无需额外配置，系统会自动加载员工邮箱配置页面。

### 4. 员工邮箱配置

管理员可以通过以下方式配置员工邮箱：

#### 方式一：管理界面配置

1. 登录管理后台
2. 进入 **资源管理** → **员工邮箱配置**
3. 选择要配置的员工，点击"配置"
4. 填写邮箱信息：
   - **邮箱地址**: example@gmail.com
   - **邮箱密码**: Gmail应用专用密码（16位）
   - **SMTP服务器**: smtp.gmail.com
   - **SMTP端口**: 587
   - **SSL加密**: 启用
   - **SMTP认证**: 启用
   - **启用个人邮箱发送**: 启用
5. 点击"保存配置"
6. 可以点击"测试"验证配置是否正确

#### 方式二：API接口配置

```bash
# 配置员工邮箱
curl -X POST "http://localhost:8080/admin/employee-email/config" \
  -H "Content-Type: application/json" \
  -d '{
    "employeeId": 1,
    "email": "employee@gmail.com",
    "emailPassword": "abcdefghijklmnop",
    "emailHost": "smtp.gmail.com",
    "emailPort": 587,
    "emailEnabled": true,
    "emailSslEnabled": true,
    "emailAuthEnabled": true
  }'

# 测试邮箱连接
curl -X POST "http://localhost:8080/admin/employee-email/test/1"

# 启用/禁用员工邮箱
curl -X PUT "http://localhost:8080/admin/employee-email/toggle/1?enabled=true"
```

## 💻 使用方法

### 在代码中使用员工邮箱发送

```java
@Autowired
private EmailService emailService;

// 使用员工邮箱发送邮件
public void sendHotelConfirmation(Long employeeId, String customerEmail, String confirmationHtml) {
    boolean success = emailService.sendEmailWithEmployeeAccount(
        employeeId,                    // 员工ID
        customerEmail,                 // 收件人
        "酒店预定确认",                // 邮件主题
        confirmationHtml,              // 邮件内容（HTML）
        null,                          // 附件内容（可选）
        null                           // 附件名称（可选）
    );
    
    if (success) {
        log.info("邮件发送成功");
    } else {
        log.error("邮件发送失败");
    }
}

// 使用员工邮箱发送带附件的邮件
public void sendHotelConfirmationWithPdf(Long employeeId, String customerEmail, 
                                        String confirmationHtml, byte[] pdfAttachment) {
    boolean success = emailService.sendEmailWithEmployeeAccount(
        employeeId,                    // 员工ID
        customerEmail,                 // 收件人
        "酒店预定确认（含确认单）",      // 邮件主题
        confirmationHtml,              // 邮件内容（HTML）
        pdfAttachment,                 // PDF附件
        "hotel-confirmation.pdf"       // 附件文件名
    );
}
```

### 测试接口

系统提供了测试接口方便验证功能：

```bash
# 测试发送简单邮件
curl -X POST "http://localhost:8080/admin/email-test/send-with-employee?employeeId=1&to=test@example.com&subject=测试邮件&body=这是测试内容"

# 测试发送带附件邮件
curl -X POST "http://localhost:8080/admin/email-test/send-with-attachment?employeeId=1&to=test@example.com"
```

## 🔧 常见邮箱服务商配置

### Gmail
- **SMTP服务器**: smtp.gmail.com
- **端口**: 587
- **SSL**: 启用
- **认证**: 启用
- **密码**: 使用应用专用密码

### Outlook/Hotmail
- **SMTP服务器**: smtp-mail.outlook.com
- **端口**: 587
- **SSL**: 启用
- **认证**: 启用

### QQ邮箱
- **SMTP服务器**: smtp.qq.com
- **端口**: 587
- **SSL**: 启用
- **认证**: 启用
- **密码**: 使用授权码

### 163邮箱
- **SMTP服务器**: smtp.163.com
- **端口**: 25 或 994（SSL）
- **SSL**: 可选
- **认证**: 启用

## 🛠️ 故障排除

### 1. 邮件发送失败

**可能原因**：
- 邮箱密码错误（Gmail需要使用应用专用密码）
- SMTP服务器配置错误
- 网络连接问题
- 邮箱服务商安全策略限制

**解决方法**：
1. 检查邮箱配置是否正确
2. 使用"测试连接"功能验证配置
3. 查看系统日志获取详细错误信息
4. 确认邮箱服务商的SMTP设置

### 2. Gmail应用专用密码问题

**错误信息**: "用户名或密码错误"

**解决方法**：
1. 确保已开启2步验证
2. 重新生成应用专用密码
3. 使用16位应用专用密码，不是登录密码

### 3. 连接超时

**可能原因**：
- 防火墙阻止SMTP连接
- 网络环境限制

**解决方法**：
1. 检查防火墙设置
2. 尝试不同的SMTP端口
3. 联系网络管理员

## 📊 系统监控

系统会记录详细的邮件发送日志，包括：

- 📧 **发送成功**: 使用员工邮箱发送成功
- ⚠️ **回退发送**: 员工邮箱失败，使用系统默认邮箱
- ❌ **发送失败**: 所有方式都失败

查看日志：
```bash
# 查看邮件发送日志
tail -f logs/sky-server.log | grep "邮件发送"
```

## 🔒 安全注意事项

1. **密码加密**: 所有邮箱密码都使用AES加密存储
2. **权限控制**: 只有管理员可以配置员工邮箱
3. **日志记录**: 详细记录邮件发送操作，便于审计
4. **密码保护**: 前端不显示已保存的邮箱密码
5. **应用专用密码**: 推荐使用应用专用密码而不是主密码

## 📈 最佳实践

1. **逐步迁移**: 先为部分员工配置，测试稳定后再全面推广
2. **备份方案**: 保持系统默认邮箱作为备份
3. **定期测试**: 定期测试员工邮箱连接状态
4. **监控日志**: 关注邮件发送成功率和错误日志
5. **培训员工**: 教育员工如何配置和维护个人邮箱设置

## 🤝 技术支持

如果在配置或使用过程中遇到问题，请：

1. 查看系统日志获取详细错误信息
2. 使用测试接口验证功能
3. 检查邮箱服务商的SMTP设置要求
4. 联系技术支持团队
