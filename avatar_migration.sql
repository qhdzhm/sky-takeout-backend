-- 为各个用户表添加头像字段的迁移脚本
-- 执行日期：2025-01-XX
-- 说明：为agents、agent_operators、employees、users表添加avatar字段

-- 1. 为agents表添加avatar字段
ALTER TABLE `agents` ADD COLUMN `avatar` VARCHAR(500) NULL COMMENT '头像URL' AFTER `email`;

-- 2. 为agent_operators表添加avatar字段
ALTER TABLE `agent_operators` ADD COLUMN `avatar` VARCHAR(500) NULL COMMENT '头像URL' AFTER `phone`;

-- 3. 为employees表添加email和avatar字段
ALTER TABLE `employees` ADD COLUMN `email` VARCHAR(100) NULL COMMENT '邮箱地址' AFTER `phone`;
ALTER TABLE `employees` ADD COLUMN `avatar` VARCHAR(500) NULL COMMENT '头像URL' AFTER `email`;

-- 4. 为users表添加avatar字段（通用头像，区别于wx_avatar微信头像）
ALTER TABLE `users` ADD COLUMN `avatar` VARCHAR(500) NULL COMMENT '头像URL' AFTER `phone`;

-- 验证字段添加情况
SELECT 
    TABLE_NAME, 
    COLUMN_NAME, 
    DATA_TYPE, 
    IS_NULLABLE, 
    COLUMN_COMMENT 
FROM 
    INFORMATION_SCHEMA.COLUMNS 
WHERE 
    TABLE_SCHEMA = 'happy_tassie_travel' 
    AND COLUMN_NAME IN ('email', 'avatar')
    AND TABLE_NAME IN ('users', 'agents', 'agent_operators', 'employees')
ORDER BY TABLE_NAME, COLUMN_NAME;