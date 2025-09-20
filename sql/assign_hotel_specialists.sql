-- 为现有酒店预订分配酒店专员
-- 执行日期: 2025-09-18

USE happy_tassie_travel;

-- 查看当前所有未分配的预订
SELECT id, booking_reference, guest_name, hotel_specialist 
FROM hotel_bookings 
WHERE hotel_specialist IS NULL;

-- 为现有预订分配酒店专员
-- 这里我们根据预订ID的奇偶数来分配给不同的专员作为示例
-- 您可以根据实际业务需求调整分配逻辑

-- 分配给 ivy（奇数ID）
UPDATE hotel_bookings 
SET hotel_specialist = 'ivy' 
WHERE hotel_specialist IS NULL AND id % 2 = 1;

-- 分配给 lisi（偶数ID）  
UPDATE hotel_bookings 
SET hotel_specialist = 'lisi' 
WHERE hotel_specialist IS NULL AND id % 2 = 0;

-- 验证分配结果
SELECT id, booking_reference, guest_name, hotel_specialist 
FROM hotel_bookings 
ORDER BY id;
