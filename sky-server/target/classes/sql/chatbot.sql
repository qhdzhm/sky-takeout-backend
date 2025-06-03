-- 聊天机器人相关表结构

-- 1. 聊天消息表
CREATE TABLE IF NOT EXISTS `chat_message` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `session_id` varchar(100) NOT NULL COMMENT '会话ID',
    `user_id` bigint DEFAULT NULL COMMENT '用户ID',
    `user_message` text NOT NULL COMMENT '用户消息',
    `bot_response` text DEFAULT NULL COMMENT '机器人回复',
    `message_type` tinyint NOT NULL DEFAULT '1' COMMENT '消息类型 1-普通问答 2-订单信息',
    `extracted_data` text DEFAULT NULL COMMENT '提取的结构化数据(JSON格式)',
    `user_type` tinyint NOT NULL DEFAULT '1' COMMENT '用户类型 1-普通客户 2-中介操作员',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    `update_time` datetime NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_session_id` (`session_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';

-- 2. 聊天会话表
CREATE TABLE IF NOT EXISTS `chat_session` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `session_id` varchar(100) NOT NULL UNIQUE COMMENT '会话ID',
    `user_id` bigint DEFAULT NULL COMMENT '用户ID',
    `user_type` tinyint NOT NULL DEFAULT '1' COMMENT '用户类型 1-普通客户 2-中介操作员',
    `status` tinyint NOT NULL DEFAULT '1' COMMENT '会话状态 1-活跃 2-结束',
    `start_time` datetime NOT NULL COMMENT '开始时间',
    `end_time` datetime DEFAULT NULL COMMENT '结束时间',
    `message_count` int NOT NULL DEFAULT '0' COMMENT '消息数量',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    `update_time` datetime NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_session_id` (`session_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天会话表';

-- 3. API使用统计表
CREATE TABLE IF NOT EXISTS `chatbot_usage` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint DEFAULT NULL COMMENT '用户ID',
    `session_id` varchar(100) NOT NULL COMMENT '会话ID',
    `api_calls` int NOT NULL DEFAULT '0' COMMENT 'API调用次数',
    `tokens_used` int NOT NULL DEFAULT '0' COMMENT '使用的token数',
    `cost` decimal(10,4) NOT NULL DEFAULT '0.0000' COMMENT '费用(美元)',
    `date` date NOT NULL COMMENT '日期',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    `update_time` datetime NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_date` (`user_id`, `date`),
    INDEX `idx_date` (`date`),
    INDEX `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='API使用统计表';

-- 4. 黑名单表
CREATE TABLE IF NOT EXISTS `chatbot_blacklist` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint DEFAULT NULL COMMENT '用户ID',
    `session_id` varchar(100) DEFAULT NULL COMMENT '会话ID',
    `ip_address` varchar(50) DEFAULT NULL COMMENT 'IP地址',
    `reason` varchar(500) NOT NULL COMMENT '拉黑原因',
    `type` tinyint NOT NULL DEFAULT '1' COMMENT '类型 1-用户 2-会话 3-IP',
    `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态 1-生效 2-失效',
    `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    `update_time` datetime NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_session_id` (`session_id`),
    INDEX `idx_ip_address` (`ip_address`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天黑名单表';

-- 表创建完成，可以通过应用程序添加测试数据 