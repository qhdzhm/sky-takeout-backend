-- 扩展员工表中的电话号码字段长度
-- 原因：取消10位手机号限制，支持员工填写多个电话号码
-- 日期：2025-10-22

USE happy_tassie_travel;

-- 扩展员工电话号码字段（从 varchar(20) 扩展到 varchar(100)）
ALTER TABLE employees 
MODIFY COLUMN phone varchar(100) DEFAULT NULL COMMENT '联系电话（支持多个号码）';

-- 验证修改结果
SELECT 
    COLUMN_NAME, 
    COLUMN_TYPE, 
    IS_NULLABLE, 
    COLUMN_DEFAULT, 
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'happy_tassie_travel' 
  AND TABLE_NAME = 'employees'
  AND COLUMN_NAME = 'phone';

