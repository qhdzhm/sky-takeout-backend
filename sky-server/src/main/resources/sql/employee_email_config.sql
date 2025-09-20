-- 为员工表添加邮箱配置字段
-- 更新员工表结构，支持个人邮箱发送

ALTER TABLE employee ADD COLUMN IF NOT EXISTS email_password VARCHAR(500); -- 加密存储的邮箱密码或应用专用密码
ALTER TABLE employee ADD COLUMN IF NOT EXISTS email_host VARCHAR(100) DEFAULT 'smtp.gmail.com';     -- SMTP服务器
ALTER TABLE employee ADD COLUMN IF NOT EXISTS email_port INT DEFAULT 587;              -- SMTP端口
ALTER TABLE employee ADD COLUMN IF NOT EXISTS email_enabled BOOLEAN DEFAULT FALSE; -- 是否启用个人邮箱发送
ALTER TABLE employee ADD COLUMN IF NOT EXISTS email_ssl_enabled BOOLEAN DEFAULT TRUE; -- 是否启用SSL
ALTER TABLE employee ADD COLUMN IF NOT EXISTS email_auth_enabled BOOLEAN DEFAULT TRUE; -- 是否需要认证

-- 添加注释
COMMENT ON COLUMN employee.email_password IS '加密存储的邮箱密码或Gmail应用专用密码';
COMMENT ON COLUMN employee.email_host IS 'SMTP服务器地址，Gmail默认为smtp.gmail.com';
COMMENT ON COLUMN employee.email_port IS 'SMTP端口，Gmail默认为587';
COMMENT ON COLUMN employee.email_enabled IS '是否启用员工个人邮箱发送，FALSE则使用系统默认邮箱';
COMMENT ON COLUMN employee.email_ssl_enabled IS '是否启用SSL加密连接';
COMMENT ON COLUMN employee.email_auth_enabled IS '是否需要SMTP认证';

-- 示例数据更新（可选）
-- UPDATE employee SET 
--   email_host = 'smtp.gmail.com',
--   email_port = 587,
--   email_ssl_enabled = TRUE,
--   email_auth_enabled = TRUE,
--   email_enabled = FALSE
-- WHERE email IS NOT NULL AND email != '';
