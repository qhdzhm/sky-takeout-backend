-- 向tour_bookings表添加成人和儿童数量字段
ALTER TABLE tour_bookings 
ADD COLUMN adult_count INT DEFAULT 0 COMMENT '成人数量' AFTER group_size,
ADD COLUMN child_count INT DEFAULT 0 COMMENT '儿童数量' AFTER adult_count; 