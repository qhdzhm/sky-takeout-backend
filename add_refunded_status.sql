-- 为 tour_bookings 表的 payment_status 字段添加 refunded 选项
-- 执行前请先备份数据库！

ALTER TABLE tour_bookings 
MODIFY COLUMN payment_status ENUM('unpaid','partial','paid','refunded') DEFAULT 'unpaid';

-- 验证修改
-- SHOW COLUMNS FROM tour_bookings LIKE 'payment_status';




