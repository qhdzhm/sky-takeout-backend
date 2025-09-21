-- 为票务预订表添加批量订票支持字段
ALTER TABLE ticket_bookings 
ADD COLUMN order_type VARCHAR(20) DEFAULT 'single' COMMENT '订单类型: single-单个订票, batch-批量订票' AFTER notes,
ADD COLUMN single_order_number VARCHAR(50) COMMENT '单个订单号(单个订票时使用)' AFTER order_type,
ADD COLUMN batch_order_numbers JSON COMMENT '批量订单号(批量订票时使用，JSON数组格式)' AFTER single_order_number,
ADD COLUMN order_count INT DEFAULT 1 COMMENT '订单数量(批量订票时记录订单数量)' AFTER batch_order_numbers;

-- 添加索引
ALTER TABLE ticket_bookings 
ADD INDEX idx_order_type (order_type),
ADD INDEX idx_single_order_number (single_order_number);

-- 显示表结构
DESCRIBE ticket_bookings;
