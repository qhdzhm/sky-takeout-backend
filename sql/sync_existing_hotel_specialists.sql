-- 同步现有数据的酒店专员字段
-- 执行日期: 2025-09-18

USE happy_tassie_travel;

-- 1. 查看当前分配情况
SELECT 
    tb.booking_id,
    tb.assigned_operator_id,
    e.username as operator_username,
    hb.id as hotel_booking_id,
    hb.booking_reference,
    hb.hotel_specialist
FROM tour_bookings tb 
LEFT JOIN employees e ON tb.assigned_operator_id = e.id 
LEFT JOIN hotel_bookings hb ON hb.tour_booking_id = tb.booking_id 
WHERE tb.assigned_operator_id IS NOT NULL 
AND hb.id IS NOT NULL;

-- 2. 同步更新酒店专员字段（基于operator_assignments表）
UPDATE hotel_bookings hb
INNER JOIN operator_assignments oa ON hb.tour_booking_id = oa.booking_id
INNER JOIN employees e ON oa.operator_id = e.id
SET hb.hotel_specialist = e.username,
    hb.updated_at = NOW()
WHERE oa.status = 'active'
AND (hb.hotel_specialist IS NULL OR hb.hotel_specialist != e.username);

-- 3. 验证同步结果
SELECT 
    tb.booking_id,
    tb.assigned_operator_id,
    e.username as operator_username,
    hb.id as hotel_booking_id,
    hb.booking_reference,
    hb.hotel_specialist
FROM tour_bookings tb 
LEFT JOIN employees e ON tb.assigned_operator_id = e.id 
LEFT JOIN hotel_bookings hb ON hb.tour_booking_id = tb.booking_id 
WHERE tb.assigned_operator_id IS NOT NULL 
AND hb.id IS NOT NULL
ORDER BY tb.booking_id;

-- 4. 统计同步结果
SELECT 
    '总的酒店预订数量' as description,
    COUNT(*) as count
FROM hotel_bookings
UNION ALL
SELECT 
    '已分配酒店专员的数量' as description,
    COUNT(*) as count
FROM hotel_bookings 
WHERE hotel_specialist IS NOT NULL
UNION ALL
SELECT 
    '未分配酒店专员的数量' as description,
    COUNT(*) as count
FROM hotel_bookings 
WHERE hotel_specialist IS NULL;
