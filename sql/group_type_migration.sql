-- =====================================
-- 团型管理功能 - 数据库迁移脚本
-- 创建时间: 2025-09-16
-- 说明: 添加团型相关字段到订单表和排团表
-- =====================================

-- 1. 备份提醒
-- 请在执行前备份相关表数据：
-- mysqldump -u root -p happy_tassie_travel tour_bookings tour_schedule_order tour_guide_vehicle_assignment > backup_before_group_type_$(date +%Y%m%d).sql

-- 2. 添加团型相关字段到订单表
-- tour_bookings 表添加团型字段
ALTER TABLE `tour_bookings` 
ADD COLUMN `group_type` enum('standard','small_12','small_14','luxury') DEFAULT 'standard' 
    COMMENT '团型：standard-普通团，small_12-12人团，small_14-14人团，luxury-精品团' 
    AFTER `status`;

ALTER TABLE `tour_bookings` 
ADD COLUMN `group_size_limit` int DEFAULT NULL 
    COMMENT '团型人数限制' 
    AFTER `group_type`;

ALTER TABLE `tour_bookings` 
ADD COLUMN `group_type_price` decimal(10,2) DEFAULT NULL 
    COMMENT '团型价格（如有调整）' 
    AFTER `group_size_limit`;

-- 添加团型索引
ALTER TABLE `tour_bookings` 
ADD INDEX `idx_group_type` (`group_type`);

-- 3. 添加团型相关字段到排团表
-- tour_schedule_order 表添加团型字段
ALTER TABLE `tour_schedule_order` 
ADD COLUMN `group_type` enum('standard','small_12','small_14','luxury') DEFAULT 'standard' 
    COMMENT '团型：standard-普通团，small_12-12人团，small_14-14人团，luxury-精品团' 
    AFTER `payment_status`;

ALTER TABLE `tour_schedule_order` 
ADD COLUMN `group_size_limit` int DEFAULT NULL 
    COMMENT '团型人数限制' 
    AFTER `group_type`;

ALTER TABLE `tour_schedule_order` 
ADD COLUMN `group_type_notes` varchar(255) DEFAULT NULL 
    COMMENT '团型特殊要求备注' 
    AFTER `group_size_limit`;

-- 添加团型索引
ALTER TABLE `tour_schedule_order` 
ADD INDEX `idx_schedule_group_type` (`group_type`);

-- 4. 添加团型相关字段到导游车辆分配表（可选）
-- tour_guide_vehicle_assignment 表添加团型字段
ALTER TABLE `tour_guide_vehicle_assignment` 
ADD COLUMN `group_type` enum('standard','small_12','small_14','luxury') DEFAULT 'standard' 
    COMMENT '团型' 
    AFTER `vehicle_info`;

ALTER TABLE `tour_guide_vehicle_assignment` 
ADD COLUMN `group_size_limit` int DEFAULT NULL 
    COMMENT '团型人数限制' 
    AFTER `group_type`;

-- 添加团型索引
ALTER TABLE `tour_guide_vehicle_assignment` 
ADD INDEX `idx_assignment_group_type` (`group_type`);

-- 5. 创建团型历史记录表（用于追踪团型变更）
DROP TABLE IF EXISTS `group_type_change_log`;

CREATE TABLE `group_type_change_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `booking_id` int NOT NULL COMMENT '订单ID',
  `old_group_type` enum('standard','small_12','small_14','luxury') DEFAULT NULL COMMENT '原团型',
  `new_group_type` enum('standard','small_12','small_14','luxury') NOT NULL COMMENT '新团型',
  `old_size_limit` int DEFAULT NULL COMMENT '原人数限制',
  `new_size_limit` int DEFAULT NULL COMMENT '新人数限制',
  `change_reason` varchar(500) DEFAULT NULL COMMENT '变更原因',
  `changed_by` bigint DEFAULT NULL COMMENT '操作人ID',
  `change_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '变更时间',
  PRIMARY KEY (`id`),
  KEY `idx_booking_id` (`booking_id`),
  KEY `idx_change_time` (`change_time`),
  CONSTRAINT `fk_group_type_log_booking` FOREIGN KEY (`booking_id`) REFERENCES `tour_bookings` (`booking_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='团型变更历史记录表';

-- 6. 初始化现有数据的团型为标准团
UPDATE `tour_bookings` SET `group_type` = 'standard' WHERE `group_type` IS NULL;
UPDATE `tour_schedule_order` SET `group_type` = 'standard' WHERE `group_type` IS NULL;
UPDATE `tour_guide_vehicle_assignment` SET `group_type` = 'standard' WHERE `group_type` IS NULL;

-- 7. 创建团型统计视图（方便后续查询）
DROP VIEW IF EXISTS `v_group_type_statistics`;

CREATE VIEW `v_group_type_statistics` AS
SELECT 
    group_type,
    COUNT(*) as total_bookings,
    SUM(total_people) as total_people,
    AVG(total_people) as avg_people_per_booking,
    SUM(CASE WHEN payment_status = 'paid' THEN 1 ELSE 0 END) as paid_bookings,
    SUM(CASE WHEN payment_status = 'paid' THEN total_price ELSE 0 END) as total_revenue,
    DATE(created_at) as booking_date
FROM tour_bookings 
WHERE created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY group_type, DATE(created_at)
ORDER BY booking_date DESC, group_type;

-- 8. 添加约束检查（确保数据一致性）
-- 检查团型和人数限制的合理性
ALTER TABLE `tour_bookings` 
ADD CONSTRAINT `chk_group_size_limit` 
CHECK (
    (group_type = 'standard' AND (group_size_limit IS NULL OR group_size_limit >= 15)) OR
    (group_type = 'small_12' AND group_size_limit <= 12) OR
    (group_type = 'small_14' AND group_size_limit <= 14) OR
    (group_type = 'luxury' AND (group_size_limit IS NULL OR group_size_limit <= 20))
);

-- 9. 创建触发器：自动记录团型变更
DROP TRIGGER IF EXISTS `tr_group_type_change_log`;

DELIMITER $$

CREATE TRIGGER `tr_group_type_change_log` 
    AFTER UPDATE ON `tour_bookings`
    FOR EACH ROW
BEGIN
    -- 检查团型是否发生变化
    IF OLD.group_type != NEW.group_type OR OLD.group_size_limit != NEW.group_size_limit THEN
        INSERT INTO `group_type_change_log` (
            booking_id, 
            old_group_type, 
            new_group_type,
            old_size_limit,
            new_size_limit,
            change_reason,
            changed_by
        ) VALUES (
            NEW.booking_id,
            OLD.group_type,
            NEW.group_type,
            OLD.group_size_limit,
            NEW.group_size_limit,
            CONCAT('团型变更: ', OLD.group_type, ' -> ', NEW.group_type),
            NEW.updated_by
        );
    END IF;
END$$

DELIMITER ;

-- 10. 验证脚本执行结果
SELECT 
    'tour_bookings' as table_name,
    COUNT(*) as total_records,
    SUM(CASE WHEN group_type = 'standard' THEN 1 ELSE 0 END) as standard_count,
    SUM(CASE WHEN group_type = 'small_12' THEN 1 ELSE 0 END) as small_12_count,
    SUM(CASE WHEN group_type = 'small_14' THEN 1 ELSE 0 END) as small_14_count,
    SUM(CASE WHEN group_type = 'luxury' THEN 1 ELSE 0 END) as luxury_count
FROM tour_bookings

UNION ALL

SELECT 
    'tour_schedule_order' as table_name,
    COUNT(*) as total_records,
    SUM(CASE WHEN group_type = 'standard' THEN 1 ELSE 0 END) as standard_count,
    SUM(CASE WHEN group_type = 'small_12' THEN 1 ELSE 0 END) as small_12_count,
    SUM(CASE WHEN group_type = 'small_14' THEN 1 ELSE 0 END) as small_14_count,
    SUM(CASE WHEN group_type = 'luxury' THEN 1 ELSE 0 END) as luxury_count
FROM tour_schedule_order;

-- 完成提示
SELECT 'Migration completed successfully! Please verify the results above.' as status;
