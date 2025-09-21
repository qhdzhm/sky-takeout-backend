-- 简化票务预订表结构，采用与导游车辆分配表相同的JSON存储方案
-- 执行时间：2025-09-20

-- 删除之前的复杂字段结构
ALTER TABLE ticket_bookings 
DROP COLUMN IF EXISTS order_type,
DROP COLUMN IF EXISTS single_order_number, 
DROP COLUMN IF EXISTS batch_order_numbers,
DROP COLUMN IF EXISTS order_count;

-- 添加简化的统一字段（参考导游车辆分配表的bookingIds和tourScheduleOrderIds字段）
ALTER TABLE ticket_bookings 
ADD COLUMN related_order_ids TEXT COMMENT '关联的订单ID列表(JSON格式，支持单个ID或多个ID)' AFTER notes,
ADD COLUMN related_order_numbers TEXT COMMENT '关联的订单号列表(JSON格式，支持单个订单号或多个订单号)' AFTER related_order_ids;

-- 添加索引以提升查询性能
ALTER TABLE ticket_bookings 
ADD INDEX idx_related_order_ids ((CAST(related_order_ids AS CHAR(255))));

-- 验证表结构
DESCRIBE ticket_bookings;

-- 查看新字段
SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'ticket_bookings' 
AND COLUMN_NAME IN ('related_order_ids', 'related_order_numbers');
