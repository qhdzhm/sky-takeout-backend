-- ========================================
-- 票务系统数据库表结构
-- 基于酒店系统架构设计
-- ========================================

-- 1. 景点表 (attractions) - 对标 hotels 表
CREATE TABLE attractions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '景点ID',
  attraction_name VARCHAR(100) NOT NULL COMMENT '景点名称',
  attraction_name_en VARCHAR(100) COMMENT '英文名称',
  location VARCHAR(200) COMMENT '位置',
  contact_phone VARCHAR(50) COMMENT '联系电话',
  contact_person VARCHAR(100) COMMENT '联系人',
  contact_email VARCHAR(200) COMMENT '联系邮箱',
  booking_type ENUM('email', 'website') NOT NULL COMMENT '预订方式: email-邮件预订, website-官网预订',
  website_url VARCHAR(500) COMMENT '官网预订地址',
  email_address VARCHAR(200) COMMENT '邮件预订地址',
  advance_days INT DEFAULT 1 COMMENT '需提前预订天数',
  description TEXT COMMENT '景点描述',
  images TEXT COMMENT '景点图片(JSON格式)',
  rating DECIMAL(3,1) COMMENT '评分(1-5)',
  status ENUM('active', 'inactive') DEFAULT 'active' COMMENT '状态: active-启用, inactive-停用',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  INDEX idx_attraction_name (attraction_name),
  INDEX idx_status (status),
  INDEX idx_booking_type (booking_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='景点信息表';

-- 2. 票务类型表 (ticket_types) - 对标 hotel_room_types 表
CREATE TABLE ticket_types (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '票务类型ID',
  attraction_id BIGINT NOT NULL COMMENT '景点ID',
  ticket_type VARCHAR(100) NOT NULL COMMENT '票务类型名称',
  ticket_type_en VARCHAR(100) COMMENT '英文名称',
  ticket_code VARCHAR(50) COMMENT '票务代码',
  base_price DECIMAL(10,2) COMMENT '基础价格',
  age_restriction VARCHAR(100) COMMENT '年龄限制',
  duration VARCHAR(50) COMMENT '游览时长',
  max_capacity INT COMMENT '最大容量',
  includes TEXT COMMENT '包含项目',
  excludes TEXT COMMENT '不包含项目',
  description TEXT COMMENT '详细描述',
  images TEXT COMMENT '票务图片(JSON格式)',
  status ENUM('active', 'inactive') DEFAULT 'active' COMMENT '状态: active-可用, inactive-停用',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  FOREIGN KEY (attraction_id) REFERENCES attractions(id) ON DELETE CASCADE,
  INDEX idx_attraction_id (attraction_id),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='票务类型表';

-- 3. 票务预订表 (ticket_bookings) - 对标 hotel_bookings 表
CREATE TABLE ticket_bookings (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '预订ID',
  booking_reference VARCHAR(100) NOT NULL UNIQUE COMMENT '预订参考号(自动生成: TB+日期+序号)',
  confirmation_number VARCHAR(100) COMMENT '确认号(景点回复的确认号)',
  schedule_order_id BIGINT COMMENT '关联的排团记录ID(可选)',
  tour_booking_id BIGINT COMMENT '关联的旅游订单ID(可选)',
  assignment_id BIGINT COMMENT '关联的导游车辆分配ID(可选)',
  attraction_id BIGINT NOT NULL COMMENT '景点ID',
  ticket_type_id BIGINT COMMENT '票务类型ID',
  guest_name VARCHAR(100) NOT NULL COMMENT '游客姓名',
  guest_phone VARCHAR(50) COMMENT '游客电话',
  guest_email VARCHAR(200) COMMENT '游客邮箱',
  visit_date DATE NOT NULL COMMENT '游览日期',
  booking_date DATE NOT NULL COMMENT '预订日期',
  adult_count INT DEFAULT 0 COMMENT '成人数',
  child_count INT DEFAULT 0 COMMENT '儿童数',
  total_guests INT NOT NULL COMMENT '游客总数(自动计算)',
  ticket_price DECIMAL(10,2) COMMENT '门票单价',
  total_amount DECIMAL(10,2) COMMENT '总金额',
  currency VARCHAR(10) DEFAULT 'AUD' COMMENT '货币类型: AUD-澳元, USD-美元, CNY-人民币',
  booking_method ENUM('email', 'website') NOT NULL COMMENT '预订方式: email-邮件预订, website-官网预订',
  booking_status ENUM('pending', 'email_sent', 'confirmed', 'visited', 'cancelled') DEFAULT 'pending' COMMENT '预订状态: pending-待预订, email_sent-已发邮件, confirmed-已确认, visited-已游览, cancelled-已取消',
  payment_status ENUM('unpaid', 'paid', 'refunded') DEFAULT 'unpaid' COMMENT '支付状态: unpaid-未支付, paid-已支付, refunded-已退款',
  special_requirements TEXT COMMENT '特殊要求',
  booking_source VARCHAR(50) DEFAULT 'system' COMMENT '预订来源: agent-代理商, direct-直接预订, system-系统预订',
  booked_by BIGINT COMMENT '预订人ID',
  ticket_specialist VARCHAR(100) COMMENT '票务专员(负责此预订的员工用户名)',
  notes TEXT COMMENT '内部备注',
  email_sent_time TIMESTAMP NULL COMMENT '邮件发送时间',
  confirmed_time TIMESTAMP NULL COMMENT '确认时间',
  visited_time TIMESTAMP NULL COMMENT '游览时间',
  cancelled_time TIMESTAMP NULL COMMENT '取消时间',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  FOREIGN KEY (attraction_id) REFERENCES attractions(id),
  FOREIGN KEY (ticket_type_id) REFERENCES ticket_types(id),
  INDEX idx_visit_date (visit_date),
  INDEX idx_booking_date (booking_date),
  INDEX idx_booking_status (booking_status),
  INDEX idx_attraction_date (attraction_id, visit_date),
  INDEX idx_guest_info (guest_name, guest_phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='票务预订表';

-- ========================================
-- 初始化数据
-- ========================================

-- 插入初始景点数据
INSERT INTO attractions (attraction_name, attraction_name_en, location, contact_phone, booking_type, website_url, email_address, description, status) VALUES
('玛丽亚岛', 'Maria Island', 'Tasmania, Australia', '+61-3-6257-0117', 'website', 'https://booking.mona.net.au/', '', '玛丽亚岛国家公园，塔斯马尼亚东海岸的自然保护区', 'active'),
('亚瑟港', 'Port Arthur Historic Site', 'Tasmania, Australia', '+61-1800-659-101', 'website', 'https://portarthurhistoricsite.com.au/', '', '亚瑟港历史遗址，澳大利亚重要的文化遗产地', 'active'),
('亚瑟港迅游', 'Port Arthur Express', 'Tasmania, Australia', '+61-3-6234-0000', 'email', '', 'bookings@portarthurexpress.com.au', '亚瑟港快速游览服务，提供便捷的游览体验', 'active');

-- 插入初始票务类型数据
INSERT INTO ticket_types (attraction_id, ticket_type, ticket_type_en, ticket_code, base_price, age_restriction, duration, description, status) VALUES
-- 玛丽亚岛票务类型
(1, '成人票', 'Adult Ticket', 'MARIA_ADULT', 25.00, '18岁以上', '全天', '玛丽亚岛成人门票，包含渡轮和岛上游览', 'active'),
(1, '儿童票', 'Child Ticket', 'MARIA_CHILD', 12.50, '6-17岁', '全天', '玛丽亚岛儿童门票，包含渡轮和岛上游览', 'active'),

-- 亚瑟港票务类型
(2, '含门票', 'Historic Site Pass', 'ARTHUR_FULL', 42.00, '全年龄', '全天', '亚瑟港历史遗址完整游览通票', 'active'),
(2, '基础票', 'Basic Entry', 'ARTHUR_BASIC', 25.00, '全年龄', '半天', '亚瑟港基础入场券', 'active'),

-- 亚瑟港迅游票务类型  
(3, '迅游套票', 'Express Package', 'ARTHUR_EXPRESS', 65.00, '全年龄', '4小时', '亚瑟港迅游套票，包含导览和交通', 'active');

-- ========================================
-- 权限和索引优化
-- ========================================

-- 添加额外索引以提高查询性能
ALTER TABLE ticket_bookings ADD INDEX idx_booking_reference (booking_reference);
ALTER TABLE ticket_bookings ADD INDEX idx_confirmation_number (confirmation_number);
ALTER TABLE ticket_bookings ADD INDEX idx_guest_email (guest_email);
ALTER TABLE ticket_bookings ADD INDEX idx_created_at (created_at);

-- ========================================
-- 执行完成提示
-- ========================================
SELECT '票务系统数据库表创建完成！' as message;

