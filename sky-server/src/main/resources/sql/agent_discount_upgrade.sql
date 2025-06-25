-- 中介折扣升级方案 - 支持每个产品针对A、B、C三档的不同折扣率
-- 每个产品都可以对A、B、C三个等级设置不同的折扣

-- 1. 中介折扣等级表
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

-- 2. 产品折扣配置表（每个产品针对每个等级的具体折扣）
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

-- 3. 更新现有agents表，添加折扣等级字段
ALTER TABLE `agents` 
ADD COLUMN `discount_level_id` bigint DEFAULT NULL COMMENT '折扣等级ID' AFTER `discount_rate`,
ADD KEY `idx_discount_level_id` (`discount_level_id`);

-- 4. 折扣计算历史记录表
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

-- 5. 插入默认折扣等级数据
INSERT INTO `agent_discount_level` (`level_code`, `level_name`, `level_description`, `sort_order`) VALUES
('A', 'A级代理', '顶级代理商，享受最高折扣', 1),
('B', 'B级代理', '高级代理商，享受较高折扣', 2),
('C', 'C级代理', '普通代理商，享受基础折扣', 3);

-- 6. 插入示例产品折扣配置数据
-- 假设一日游产品ID: 1, 2, 3，跟团游产品ID: 1, 2, 3
-- 你需要根据实际的产品ID来调整这些数据

-- 一日游产品1的折扣配置
INSERT INTO `product_agent_discount` (`product_type`, `product_id`, `level_id`, `discount_rate`) VALUES
('day_tour', 1, 1, 0.70),  -- A级代理对一日游产品1享受30%折扣
('day_tour', 1, 2, 0.80),  -- B级代理对一日游产品1享受20%折扣  
('day_tour', 1, 3, 0.90);  -- C级代理对一日游产品1享受10%折扣

-- 一日游产品2的折扣配置
INSERT INTO `product_agent_discount` (`product_type`, `product_id`, `level_id`, `discount_rate`) VALUES
('day_tour', 2, 1, 0.75),  -- A级代理对一日游产品2享受25%折扣
('day_tour', 2, 2, 0.85),  -- B级代理对一日游产品2享受15%折扣
('day_tour', 2, 3, 0.92);  -- C级代理对一日游产品2享受8%折扣

-- 跟团游产品1的折扣配置  
INSERT INTO `product_agent_discount` (`product_type`, `product_id`, `level_id`, `discount_rate`) VALUES
('group_tour', 1, 1, 0.65), -- A级代理对跟团游产品1享受35%折扣
('group_tour', 1, 2, 0.75), -- B级代理对跟团游产品1享受25%折扣
('group_tour', 1, 3, 0.88); -- C级代理对跟团游产品1享受12%折扣

-- 跟团游产品2的折扣配置
INSERT INTO `product_agent_discount` (`product_type`, `product_id`, `level_id`, `discount_rate`) VALUES
('group_tour', 2, 1, 0.72), -- A级代理对跟团游产品2享受28%折扣
('group_tour', 2, 2, 0.82), -- B级代理对跟团游产品2享受18%折扣  
('group_tour', 2, 3, 0.90); -- C级代理对跟团游产品2享受10%折扣

-- 7. 查询SQL示例
-- 查询某个中介对某个产品的折扣率
/*
SELECT 
    pad.discount_rate,
    adl.level_name,
    pad.product_type,
    pad.product_id
FROM product_agent_discount pad
JOIN agent_discount_level adl ON pad.level_id = adl.id
JOIN agents a ON a.discount_level_id = adl.id
WHERE a.id = ? -- 中介ID
  AND pad.product_type = ? -- 产品类型
  AND pad.product_id = ? -- 产品ID
  AND pad.is_active = 1
  AND adl.is_active = 1
  AND (pad.valid_until IS NULL OR pad.valid_until > NOW());
*/

-- 8. 批量插入SQL模板（你可以根据实际产品数据来批量插入）
/*
-- 为所有一日游产品设置折扣（假设有day_tour_id从1到100）
INSERT INTO `product_agent_discount` (`product_type`, `product_id`, `level_id`, `discount_rate`)
SELECT 'day_tour', dt.day_tour_id, 1, 0.75 FROM day_tours dt  -- A级代理25%折扣
UNION ALL
SELECT 'day_tour', dt.day_tour_id, 2, 0.85 FROM day_tours dt  -- B级代理15%折扣  
UNION ALL
SELECT 'day_tour', dt.day_tour_id, 3, 0.90 FROM day_tours dt; -- C级代理10%折扣

-- 为所有跟团游产品设置折扣
INSERT INTO `product_agent_discount` (`product_type`, `product_id`, `level_id`, `discount_rate`)
SELECT 'group_tour', gt.group_tour_id, 1, 0.70 FROM group_tours gt  -- A级代理30%折扣
UNION ALL  
SELECT 'group_tour', gt.group_tour_id, 2, 0.80 FROM group_tours gt  -- B级代理20%折扣
UNION ALL
SELECT 'group_tour', gt.group_tour_id, 3, 0.88 FROM group_tours gt; -- C级代理12%折扣
*/ 