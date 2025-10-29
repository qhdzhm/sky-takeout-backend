-- ============================================
-- 统一B级代理商的折扣率
-- ============================================

USE happy_tassie_travel;

-- 查看当前所有B级代理商的折扣率
SELECT 
    id AS '代理商ID',
    company_name AS '公司名称',
    discount_rate AS '当前折扣率',
    discount_level_id AS '折扣等级ID'
FROM agents
WHERE discount_level_id = 2
ORDER BY id;

-- ============================================
-- 选项1: 将所有B级代理商统一设置为0.75折扣率
-- ============================================
-- UPDATE agents 
-- SET discount_rate = 0.75
-- WHERE discount_level_id = 2;

-- ============================================
-- 选项2: 只修改代理商27的折扣率（与代理商29保持一致）
-- ============================================
-- UPDATE agents 
-- SET discount_rate = 0.75
-- WHERE id = 27;

-- ============================================
-- 选项3: 将代理商29的折扣率改为1.00（与代理商27保持一致）
-- ============================================
-- UPDATE agents 
-- SET discount_rate = 1.00
-- WHERE id = 29;

-- 验证修改结果
-- SELECT 
--     id AS '代理商ID',
--     company_name AS '公司名称',
--     discount_rate AS '新折扣率',
--     discount_level_id AS '折扣等级ID'
-- FROM agents
-- WHERE discount_level_id = 2
-- ORDER BY id;

