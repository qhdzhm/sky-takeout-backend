-- =====================================================
-- 中介折扣升级方案 - 完整版
-- 支持每个产品针对A、B、C三档的不同折扣率
-- =====================================================

-- 1. 创建中介折扣等级表
CREATE TABLE IF NOT EXISTS `agent_discount_level` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `level_code` varchar(10) NOT NULL COMMENT '等级代码：A、B、C',
    `level_name` varchar(50) NOT NULL COMMENT '等级名称：如A级代理、B级代理、C级代理',
    `level_description` varchar(200) DEFAULT NULL COMMENT '等级描述',
    `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序顺序，数字越小等级越高',
    `is_active` tinyint NOT NULL DEFAULT 1 COMMENT '是否激活：1-激活，0-停用',
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_level_code` (`level_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='中介折扣等级表';

-- 2. 创建产品折扣配置表（每个产品针对每个等级的具体折扣）
CREATE TABLE IF NOT EXISTS `product_agent_discount` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `product_type` varchar(20) NOT NULL COMMENT '产品类型：day_tour-一日游，group_tour-跟团游',
    `product_id` bigint NOT NULL COMMENT '产品ID',
    `level_id` bigint NOT NULL COMMENT '折扣等级ID',
    `discount_rate` decimal(5,4) NOT NULL COMMENT '折扣率：0.85表示85%即15%折扣',
    `min_order_amount` decimal(10,2) DEFAULT NULL COMMENT '最小订单金额限制',
    `max_discount_amount` decimal(10,2) DEFAULT NULL COMMENT '最大折扣金额限制',
    `valid_from` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生效开始时间',
    `valid_until` datetime DEFAULT NULL COMMENT '生效结束时间，NULL表示永久有效',
    `is_active` tinyint NOT NULL DEFAULT 1 COMMENT '是否激活：1-激活，0-停用',
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_product_level` (`product_type`, `product_id`, `level_id`),
    KEY `idx_product` (`product_type`, `product_id`),
    KEY `idx_level_id` (`level_id`),
    KEY `idx_valid_time` (`valid_from`, `valid_until`),
    FOREIGN KEY (`level_id`) REFERENCES `agent_discount_level` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='产品中介折扣配置表';

-- 3. 创建折扣计算历史记录表
CREATE TABLE IF NOT EXISTS `agent_discount_log` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `agent_id` bigint NOT NULL COMMENT '中介ID',
    `order_id` bigint DEFAULT NULL COMMENT '订单ID',
    `product_type` varchar(20) NOT NULL COMMENT '产品类型',
    `product_id` bigint NOT NULL COMMENT '产品ID',
    `original_price` decimal(10,2) NOT NULL COMMENT '原价',
    `discount_rate` decimal(5,4) NOT NULL COMMENT '实际使用的折扣率',
    `discount_amount` decimal(10,2) NOT NULL COMMENT '折扣金额',
    `final_price` decimal(10,2) NOT NULL COMMENT '最终价格',
    `level_code` varchar(10) NOT NULL COMMENT '使用的折扣等级',
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_agent_id` (`agent_id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_product` (`product_type`, `product_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='中介折扣计算历史记录表';

-- 4. 更新现有agents表，添加折扣等级字段
ALTER TABLE `agents` 
ADD COLUMN `discount_level_id` bigint DEFAULT NULL COMMENT '折扣等级ID' AFTER `discount_rate`,
ADD KEY `idx_discount_level_id` (`discount_level_id`);

-- =====================================================
-- 测试数据插入
-- =====================================================

-- 5. 插入折扣等级数据
INSERT INTO `agent_discount_level` (`level_code`, `level_name`, `level_description`, `sort_order`) VALUES
('A', 'A级代理', '顶级代理商，享受最高折扣，年销售额100万以上', 1),
('B', 'B级代理', '高级代理商，享受较高折扣，年销售额50-100万', 2),
('C', 'C级代理', '普通代理商，享受基础折扣，年销售额50万以下', 3);

-- 6. 批量为所有一日游产品设置折扣（根据实际day_tours表数据）
INSERT INTO `product_agent_discount` (`product_type`, `product_id`, `level_id`, `discount_rate`)
SELECT 'day_tour', dt.day_tour_id, 1, 0.70 FROM day_tours dt  -- A级代理30%折扣
UNION ALL
SELECT 'day_tour', dt.day_tour_id, 2, 0.80 FROM day_tours dt  -- B级代理20%折扣  
UNION ALL
SELECT 'day_tour', dt.day_tour_id, 3, 0.90 FROM day_tours dt; -- C级代理10%折扣

-- 7. 批量为所有跟团游产品设置折扣（根据实际group_tours表数据）
INSERT INTO `product_agent_discount` (`product_type`, `product_id`, `level_id`, `discount_rate`)
SELECT 'group_tour', gt.group_tour_id, 1, 0.65 FROM group_tours gt  -- A级代理35%折扣
UNION ALL  
SELECT 'group_tour', gt.group_tour_id, 2, 0.75 FROM group_tours gt  -- B级代理25%折扣
UNION ALL
SELECT 'group_tour', gt.group_tour_id, 3, 0.85 FROM group_tours gt; -- C级代理15%折扣

-- 8. 为特定产品设置不同的折扣率（示例：某些热门产品折扣更低）
-- 假设一日游产品ID=1是热门产品，给更低的折扣
INSERT INTO `product_agent_discount` (`product_type`, `product_id`, `level_id`, `discount_rate`) VALUES
('day_tour', 1, 1, 0.75),  -- A级代理对热门一日游1只有25%折扣
('day_tour', 1, 2, 0.85),  -- B级代理对热门一日游1只有15%折扣
('day_tour', 1, 3, 0.95)   -- C级代理对热门一日游1只有5%折扣
ON DUPLICATE KEY UPDATE 
    discount_rate = VALUES(discount_rate),
    updated_at = CURRENT_TIMESTAMP;

-- 假设跟团游产品ID=2是热门产品，给更低的折扣
INSERT INTO `product_agent_discount` (`product_type`, `product_id`, `level_id`, `discount_rate`) VALUES
('group_tour', 2, 1, 0.72), -- A级代理对热门跟团游2只有28%折扣
('group_tour', 2, 2, 0.82), -- B级代理对热门跟团游2只有18%折扣
('group_tour', 2, 3, 0.90)  -- C级代理对热门跟团游2只有10%折扣
ON DUPLICATE KEY UPDATE 
    discount_rate = VALUES(discount_rate),
    updated_at = CURRENT_TIMESTAMP;

-- 9. 更新现有代理商的等级（示例数据）
-- 假设代理商ID=1是A级，ID=2是B级，ID=3是C级
UPDATE `agents` SET `discount_level_id` = 1 WHERE `id` = 1; -- 设置为A级
UPDATE `agents` SET `discount_level_id` = 2 WHERE `id` = 2; -- 设置为B级  
UPDATE `agents` SET `discount_level_id` = 3 WHERE `id` = 3; -- 设置为C级

-- =====================================================
-- 查询SQL示例
-- =====================================================

-- 10. 查询某个中介对某个产品的折扣率
-- SELECT 
--     a.id as agent_id,
--     a.company_name,
--     adl.level_name,
--     pad.product_type,
--     pad.product_id,
--     pad.discount_rate,
--     (1 - pad.discount_rate) * 100 as discount_percentage
-- FROM agents a
-- JOIN agent_discount_level adl ON a.discount_level_id = adl.id
-- JOIN product_agent_discount pad ON pad.level_id = adl.id
-- WHERE a.id = 1 -- 代理商ID
--   AND pad.product_type = 'day_tour' -- 产品类型
--   AND pad.product_id = 1 -- 产品ID
--   AND pad.is_active = 1
--   AND adl.is_active = 1
--   AND (pad.valid_until IS NULL OR pad.valid_until > NOW());

-- 11. 查询所有代理商等级及其对应的产品折扣统计
-- SELECT 
--     adl.level_code,
--     adl.level_name,
--     COUNT(pad.id) as total_products,
--     AVG(pad.discount_rate) as avg_discount_rate,
--     MIN(pad.discount_rate) as min_discount_rate,
--     MAX(pad.discount_rate) as max_discount_rate
-- FROM agent_discount_level adl
-- LEFT JOIN product_agent_discount pad ON adl.id = pad.level_id AND pad.is_active = 1
-- WHERE adl.is_active = 1
-- GROUP BY adl.id, adl.level_code, adl.level_name
-- ORDER BY adl.sort_order;

-- 12. 查询某个代理商可以享受的所有产品折扣
-- SELECT 
--     a.company_name as agent_name,
--     adl.level_name,
--     pad.product_type,
--     pad.product_id,
--     CASE 
--         WHEN pad.product_type = 'day_tour' THEN dt.name
--         WHEN pad.product_type = 'group_tour' THEN gt.title
--     END as product_name,
--     pad.discount_rate,
--     (1 - pad.discount_rate) * 100 as discount_percentage
-- FROM agents a
-- JOIN agent_discount_level adl ON a.discount_level_id = adl.id
-- JOIN product_agent_discount pad ON pad.level_id = adl.id
-- LEFT JOIN day_tours dt ON pad.product_type = 'day_tour' AND pad.product_id = dt.day_tour_id
-- LEFT JOIN group_tours gt ON pad.product_type = 'group_tour' AND pad.product_id = gt.group_tour_id
-- WHERE a.id = 1 -- 代理商ID
--   AND pad.is_active = 1
--   AND adl.is_active = 1
--   AND (pad.valid_until IS NULL OR pad.valid_until > NOW())
-- ORDER BY pad.product_type, pad.product_id;

-- =====================================================
-- 管理功能SQL示例
-- =====================================================

-- 13. 批量调整某个等级对某类产品的折扣率
-- UPDATE product_agent_discount 
-- SET discount_rate = 0.68, updated_at = CURRENT_TIMESTAMP
-- WHERE level_id = 1 -- A级代理
--   AND product_type = 'group_tour' -- 跟团游
--   AND is_active = 1;

-- 14. 为新产品批量添加折扣配置
-- INSERT INTO `product_agent_discount` (`product_type`, `product_id`, `level_id`, `discount_rate`)
-- VALUES 
-- ('day_tour', 999, 1, 0.70),  -- 新产品ID=999的A级折扣
-- ('day_tour', 999, 2, 0.80),  -- 新产品ID=999的B级折扣
-- ('day_tour', 999, 3, 0.90);  -- 新产品ID=999的C级折扣

-- 数据插入完成！ 