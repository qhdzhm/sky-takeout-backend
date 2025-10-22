-- ================================================================
-- 订单预订状态查询性能优化 - 添加索引
-- 用于订单列表快速显示酒店/票务预订状态
-- ================================================================

-- 1. 检查 hotel_bookings 表的 tour_booking_id 索引
-- 如果索引已存在，这条语句会报错但不影响后续操作
SELECT 
    TABLE_NAME,
    INDEX_NAME,
    COLUMN_NAME
FROM 
    INFORMATION_SCHEMA.STATISTICS
WHERE 
    TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'hotel_bookings'
    AND COLUMN_NAME = 'tour_booking_id';

-- 2. 创建 hotel_bookings.tour_booking_id 索引（如果不存在）
-- 用于快速查询某订单是否有酒店预订
CREATE INDEX IF NOT EXISTS idx_hotel_tour_booking_id 
ON hotel_bookings(tour_booking_id);

-- 3. 检查 ticket_bookings 表的 tour_booking_id 索引
SELECT 
    TABLE_NAME,
    INDEX_NAME,
    COLUMN_NAME
FROM 
    INFORMATION_SCHEMA.STATISTICS
WHERE 
    TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'ticket_bookings'
    AND COLUMN_NAME = 'tour_booking_id';

-- 4. 创建 ticket_bookings.tour_booking_id 索引（如果不存在）
-- 用于快速查询某订单是否有票务预订
CREATE INDEX IF NOT EXISTS idx_ticket_tour_booking_id 
ON ticket_bookings(tour_booking_id);

-- 5. 验证索引创建结果
SELECT 
    TABLE_NAME,
    INDEX_NAME,
    COLUMN_NAME,
    SEQ_IN_INDEX,
    CARDINALITY
FROM 
    INFORMATION_SCHEMA.STATISTICS
WHERE 
    TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME IN ('hotel_bookings', 'ticket_bookings')
    AND COLUMN_NAME = 'tour_booking_id'
ORDER BY 
    TABLE_NAME, INDEX_NAME;

-- ================================================================
-- 说明：
-- 1. 这两个索引会显著提升订单列表查询性能
-- 2. 对于百万级数据，查询时间从秒级降至毫秒级
-- 3. 索引占用空间小，对写入性能影响微乎其微
-- ================================================================




