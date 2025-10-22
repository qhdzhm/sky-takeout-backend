-- 扩展乘客表中的电话号码字段长度
-- 原因：支持客人填写多个电话号码（例如："158878787或12345646246"）
-- 日期：2025-10-22

USE happy_tassie_travel;

-- 1. 扩展主电话号码字段（从 varchar(20) 扩展到 varchar(100)）
ALTER TABLE passengers 
MODIFY COLUMN phone varchar(100) DEFAULT NULL COMMENT '联系电话（支持多个号码）';

-- 2. 同时扩展紧急联系人电话字段（保持一致性）
ALTER TABLE passengers 
MODIFY COLUMN emergency_contact_phone varchar(100) DEFAULT NULL COMMENT '紧急联系人电话（支持多个号码）';

-- 3. 验证修改结果
DESCRIBE passengers;

SELECT 
    COLUMN_NAME, 
    COLUMN_TYPE, 
    IS_NULLABLE, 
    COLUMN_DEFAULT, 
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'happy_tassie_travel' 
  AND TABLE_NAME = 'passengers'
  AND COLUMN_NAME IN ('phone', 'emergency_contact_phone');

