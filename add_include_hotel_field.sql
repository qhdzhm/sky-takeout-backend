-- =============================================
-- 添加"是否包含酒店"字段到订单表和拍团表
-- 创建日期: 2025-01-21
-- 功能说明: 
--   1. 支持中介下单时选择不包含酒店
--   2. 不包含酒店时，酒店相关字段可以为空
--   3. 价格计算会跳过酒店费用
--   4. 拍团表也需要同步该字段
-- =============================================

USE happy_tassie_travel;

-- ① 为订单表 tour_bookings 添加 include_hotel 字段
ALTER TABLE tour_bookings 
ADD COLUMN include_hotel TINYINT(1) NOT NULL DEFAULT 1 
COMMENT '是否包含酒店：1=包含，0=不包含'
AFTER hotel_level;

-- ② 为拍团表 tour_schedule_order 添加 include_hotel 字段
ALTER TABLE tour_schedule_order 
ADD COLUMN include_hotel TINYINT(1) NOT NULL DEFAULT 1 
COMMENT '是否包含酒店：1=包含，0=不包含'
AFTER hotel_level;

-- 验证字段是否添加成功
SELECT 
    'tour_bookings' as table_name,
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'happy_tassie_travel'
  AND TABLE_NAME = 'tour_bookings'
  AND COLUMN_NAME = 'include_hotel';

SELECT 
    'tour_schedule_order' as table_name,
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'happy_tassie_travel'
  AND TABLE_NAME = 'tour_schedule_order'
  AND COLUMN_NAME = 'include_hotel';

-- 说明：
-- 1. 默认值为 1（包含酒店），保持向后兼容
-- 2. 现有订单会自动设置为包含酒店
-- 3. 当 include_hotel = 0 时：
--    - 价格计算不包含酒店费用
--    - 酒店相关字段（hotel_level, room_type等）可以为NULL
--    - 拍团表的接送地点需要后台手动填写（无法自动从酒店地址获取）
-- 4. 支付后订单同步到拍团表时，include_hotel 字段会一并同步


