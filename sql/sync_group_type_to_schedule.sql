-- =====================================
-- 团型信息同步脚本
-- 创建时间: 2025-09-16
-- 说明: 将订单表中的团型信息同步到排团表
-- =====================================

-- 查看当前数据状态
SELECT 
    '========== 同步前数据状态 ==========' as status_info;

-- 查看订单表中有团型信息的记录
SELECT 
    COUNT(*) as total_bookings_with_group_type
FROM tour_bookings 
WHERE group_type IS NOT NULL AND group_type != 'standard';

-- 查看排团表中有团型信息的记录
SELECT 
    COUNT(*) as total_schedule_with_group_type
FROM tour_schedule_order 
WHERE group_type IS NOT NULL AND group_type != 'standard';

-- 查看需要同步的数据
SELECT 
    tb.booking_id,
    tb.order_number,
    tb.group_type as booking_group_type,
    tb.group_size_limit as booking_size_limit,
    tso.group_type as schedule_group_type,
    tso.group_size_limit as schedule_size_limit
FROM tour_bookings tb
INNER JOIN tour_schedule_order tso ON tb.booking_id = tso.booking_id
WHERE tb.group_type IS NOT NULL 
  AND tb.group_type != 'standard'
  AND (tso.group_type IS NULL OR tso.group_type = 'standard' OR tso.group_type != tb.group_type)
LIMIT 10;

-- 执行同步操作
UPDATE tour_schedule_order tso
INNER JOIN tour_bookings tb ON tso.booking_id = tb.booking_id
SET 
    tso.group_type = tb.group_type,
    tso.group_size_limit = tb.group_size_limit,
    tso.updated_at = NOW()
WHERE tb.group_type IS NOT NULL 
  AND tb.group_type != 'standard'
  AND (tso.group_type IS NULL OR tso.group_type = 'standard');

-- 查看同步结果
SELECT 
    '========== 同步后数据状态 ==========' as status_info;

-- 查看排团表中有团型信息的记录（同步后）
SELECT 
    COUNT(*) as total_schedule_with_group_type_after_sync
FROM tour_schedule_order 
WHERE group_type IS NOT NULL AND group_type != 'standard';

-- 验证同步结果
SELECT 
    tb.booking_id,
    tb.order_number,
    tb.group_type as booking_group_type,
    tb.group_size_limit as booking_size_limit,
    tso.group_type as schedule_group_type,
    tso.group_size_limit as schedule_size_limit,
    CASE 
        WHEN tb.group_type = tso.group_type AND tb.group_size_limit = tso.group_size_limit 
        THEN '✅ 同步成功'
        ELSE '❌ 同步失败'
    END as sync_status
FROM tour_bookings tb
INNER JOIN tour_schedule_order tso ON tb.booking_id = tso.booking_id
WHERE tb.group_type IS NOT NULL 
  AND tb.group_type != 'standard'
ORDER BY tb.booking_id DESC
LIMIT 10;

-- 统计信息
SELECT 
    '========== 同步统计信息 ==========' as status_info;

SELECT 
    tso.group_type,
    COUNT(*) as count
FROM tour_schedule_order tso
WHERE tso.group_type IS NOT NULL
GROUP BY tso.group_type
ORDER BY count DESC;