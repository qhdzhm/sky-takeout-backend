-- 添加酒店预订号字段
-- 执行日期: 2025-09-18

USE happy_tassie_travel;

-- 1. 在hotel_bookings表中添加酒店预订号字段
ALTER TABLE hotel_bookings 
ADD COLUMN IF NOT EXISTS hotel_booking_number VARCHAR(100) DEFAULT NULL COMMENT '酒店预订号' 
AFTER booking_reference;

-- 2. 在tour_schedule_order表中添加酒店预订号字段
ALTER TABLE tour_schedule_order 
ADD COLUMN IF NOT EXISTS hotel_booking_number VARCHAR(100) DEFAULT NULL COMMENT '酒店预订号' 
AFTER room_details;

-- 3. 为hotel_booking_number字段添加索引以提高查询性能
CREATE INDEX IF NOT EXISTS idx_hotel_bookings_booking_number ON hotel_bookings (hotel_booking_number);
CREATE INDEX IF NOT EXISTS idx_tour_schedule_hotel_booking_number ON tour_schedule_order (hotel_booking_number);

-- 4. 验证字段添加成功
SELECT 'hotel_bookings表结构验证:' as info;
DESCRIBE hotel_bookings;

SELECT 'tour_schedule_order表结构验证:' as info;
DESCRIBE tour_schedule_order;
