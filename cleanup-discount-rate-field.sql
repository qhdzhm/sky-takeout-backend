-- ============================================
-- 清理不再使用的 discount_rate 字段（可选）
-- ============================================
-- 说明：
-- 由于我们已经改用纯折扣等级系统（product_agent_discount表），
-- agents.discount_rate 字段不再使用。
-- 
-- 如果确认系统运行正常，可以执行此脚本删除该字段。
-- 建议：先运行一段时间确认无问题后再删除。
-- ============================================

USE happy_tassie_travel;

-- 备份当前的 discount_rate 数据（以防万一）
CREATE TABLE IF NOT EXISTS agents_discount_rate_backup AS
SELECT id, company_name, discount_rate, discount_level_id, created_at
FROM agents;

SELECT '✅ 已备份 discount_rate 数据到 agents_discount_rate_backup 表' AS message;

-- 查看当前使用情况
SELECT 
    '当前代理商数量' AS stat_name,
    COUNT(*) AS count
FROM agents
UNION ALL
SELECT 
    '有折扣等级的代理商',
    COUNT(*)
FROM agents
WHERE discount_level_id IS NOT NULL
UNION ALL
SELECT 
    '没有折扣等级的代理商',
    COUNT(*)
FROM agents
WHERE discount_level_id IS NULL;

-- 警告：如果有代理商没有设置折扣等级，请先为他们设置等级！
-- 
-- 如果上面的统计显示所有代理商都已有折扣等级，
-- 可以取消注释下面的语句来删除 discount_rate 字段：
--
-- ALTER TABLE agents DROP COLUMN discount_rate;
-- SELECT '✅ discount_rate 字段已删除' AS message;

