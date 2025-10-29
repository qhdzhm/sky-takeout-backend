-- Add small group related fields to tour_bookings table
-- 为tour_bookings表添加小团相关字段

USE happy_tassie_travel;

-- Add is_small_group and small_group_extra_fee to tour_bookings
ALTER TABLE tour_bookings
ADD COLUMN is_small_group TINYINT(1) DEFAULT 0 COMMENT '是否为小团，0-否，1-是',
ADD COLUMN small_group_extra_fee DECIMAL(10, 2) DEFAULT 0.00 COMMENT '小团额外费用总计';

-- Verify the changes
SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'happy_tassie_travel' 
  AND TABLE_NAME = 'tour_bookings' 
  AND COLUMN_NAME IN ('is_small_group', 'small_group_extra_fee');



