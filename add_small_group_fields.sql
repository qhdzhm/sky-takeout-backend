-- 为跟团游和一日游表添加小团差价字段
-- 执行时间：根据实际需要

-- 1. 为group_tours表添加小团差价字段
ALTER TABLE group_tours 
ADD COLUMN small_group_price_difference DECIMAL(10,2) DEFAULT 0.00 COMMENT '小团差价（每人每天）' AFTER guide_fee;

-- 2. 为day_tours表添加小团差价字段  
ALTER TABLE day_tours 
ADD COLUMN small_group_price_difference DECIMAL(10,2) DEFAULT 0.00 COMMENT '小团差价（每人）' AFTER guide_fee;

-- 3. 更新现有产品的小团差价（根据实际情况调整）
-- 示例：假设精品小团每人每天加100
UPDATE group_tours 
SET small_group_price_difference = 100.00 
WHERE title LIKE '%精品%' OR title LIKE '%小团%' OR title LIKE '%14人%' OR title LIKE '%12人%';

-- 4. 查看更新结果
SELECT group_tour_id, title, price, small_group_price_difference 
FROM group_tours 
WHERE show_on_user_site = 1 
ORDER BY group_tour_id;



