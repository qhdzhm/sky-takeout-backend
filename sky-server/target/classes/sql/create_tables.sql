-- 车辆驾驶员关联表
CREATE TABLE IF NOT EXISTS `vehicle_driver` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `vehicle_id` bigint(20) NOT NULL COMMENT '车辆ID',
  `employee_id` bigint(20) NOT NULL COMMENT '员工ID',
  `is_primary` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否为主驾驶：0-副驾驶，1-主驾驶',
  `assign_time` datetime DEFAULT NULL COMMENT '分配时间',
  `unassign_time` datetime DEFAULT NULL COMMENT '取消分配时间',
  `notes` varchar(255) COLLATE utf8_bin DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_vehicle_id` (`vehicle_id`),
  KEY `idx_employee_id` (`employee_id`),
  UNIQUE KEY `uk_vehicle_employee` (`vehicle_id`,`employee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='车辆驾驶员关联表'; 