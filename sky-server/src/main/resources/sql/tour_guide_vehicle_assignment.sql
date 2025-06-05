-- 导游车辆游客分配表
-- 用于保存分配后的详细信息，类似图2的表格数据
CREATE TABLE IF NOT EXISTS `tour_guide_vehicle_assignment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `assignment_date` date NOT NULL COMMENT '分配日期',
  `destination` varchar(255) NOT NULL COMMENT '目的地/地点',
  `guide_id` bigint(20) NOT NULL COMMENT '导游ID',
  `guide_name` varchar(100) NOT NULL COMMENT '导游姓名',
  `vehicle_id` bigint(20) NOT NULL COMMENT '车辆ID',
  `license_plate` varchar(20) NOT NULL COMMENT '车牌号',
  `vehicle_type` varchar(50) NOT NULL COMMENT '车辆类型',
  `total_people` int(11) NOT NULL COMMENT '总人数',
  `adult_count` int(11) NOT NULL DEFAULT '0' COMMENT '成人数量',
  `child_count` int(11) NOT NULL DEFAULT '0' COMMENT '儿童数量',
  `contact_phone` varchar(20) NOT NULL COMMENT '联系方式',
  `contact_person` varchar(100) NOT NULL COMMENT '联系人姓名',
  `pickup_method` varchar(50) DEFAULT NULL COMMENT '接送方式',
  `pickup_location` varchar(255) DEFAULT NULL COMMENT '接送地点',
  `dropoff_location` varchar(255) DEFAULT NULL COMMENT '送达地点',
  `remarks` text DEFAULT NULL COMMENT '备注信息',
  `next_destination` varchar(255) DEFAULT NULL COMMENT '下一站信息',
  `status` varchar(20) NOT NULL DEFAULT 'confirmed' COMMENT '状态：confirmed-已确认，in_progress-进行中，completed-已完成，cancelled-已取消',
  `booking_ids` text DEFAULT NULL COMMENT '关联的订单ID列表(JSON格式)',
  `tour_schedule_order_ids` text DEFAULT NULL COMMENT '关联的行程排序ID列表(JSON格式)',
  `passenger_details` text DEFAULT NULL COMMENT '游客详细信息列表(JSON格式)',
  `special_requirements` text DEFAULT NULL COMMENT '特殊要求汇总',
  `dietary_restrictions` text DEFAULT NULL COMMENT '饮食限制汇总',
  `luggage_info` varchar(255) DEFAULT NULL COMMENT '行李信息',
  `emergency_contact` varchar(255) DEFAULT NULL COMMENT '紧急联系人',
  `language_preference` varchar(50) DEFAULT NULL COMMENT '语言偏好',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint(20) DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint(20) DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  KEY `idx_assignment_date` (`assignment_date`),
  KEY `idx_guide_id` (`guide_id`),
  KEY `idx_vehicle_id` (`vehicle_id`),
  KEY `idx_destination` (`destination`),
  KEY `idx_status` (`status`),
  KEY `idx_contact_person` (`contact_person`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='导游车辆游客分配表';

-- 插入测试数据
INSERT INTO `tour_guide_vehicle_assignment` (
  `assignment_date`, `destination`, `guide_id`, `guide_name`, `vehicle_id`, 
  `license_plate`, `vehicle_type`, `total_people`, `adult_count`, `child_count`,
  `contact_phone`, `contact_person`, `pickup_method`, `pickup_location`, 
  `dropoff_location`, `remarks`, `next_destination`, `status`, 
  `booking_ids`, `passenger_details`, `special_requirements`, 
  `created_by`, `updated_by`
) VALUES 
(
  '2025-05-31', '布鲁尼岛', 1, '张导游', 1, 
  'XT504H', '25座 mini+', 17, 15, 2,
  '0432421349', '刘嘉诚', 'Ironcreek', '28-31/05 1T Ironcreek #27643', 
  'Ironcreek', '去bonorong', 'bonorong', 'confirmed', 
  '[1,2,3]', '[{"name":"刘嘉诚","age":35,"requirements":"无"},{"name":"李明","age":28,"requirements":"素食"}]', '素食需求', 
  1, 1
),
(
  '2025-05-31', '酒杯湾', 2, '李导游', 2, 
  'HZL190511', '商务车', 2, 2, 0,
  '1686778565', '王静云', 'Ironcreek', '27-31/05 1D Ironcreek #27586', 
  'Wineglass Bay', '请准备防晒用品', 'Wineglass Bay', 'confirmed', 
  '[4,5]', '[{"name":"王静云","age":32,"requirements":"无"},{"name":"张华","age":29,"requirements":"无"}]', '无特殊要求', 
  1, 1
),
(
  '2025-05-31', '摇篮山', 3, '陈导游', 3, 
  'HZL096507', '中巴', 2, 2, 0,
  '1845412916', '刘诗音', 'Ironcreek', '28/05-01/06 1T Ironcreek #27465', 
  'Cradle Mountain', '带好保暖衣物', 'Wineglass Bay', 'confirmed', 
  '[6,7]', '[{"name":"刘诗音","age":26,"requirements":"晕车"},{"name":"赵明","age":30,"requirements":"无"}]', '晕车药品准备', 
  1, 1
); 