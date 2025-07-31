package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 聊天消息实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    
    private Long id;
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 用户ID - 支持数字ID和字符串ID（如guest用户）
     */
    private String userId;
    
    /**
     * 用户消息
     */
    private String userMessage;
    
    /**
     * 机器人回复
     */
    private String botResponse;
    
    /**
     * 消息类型 1-普通问答 2-订单信息
     */
    private Integer messageType;
    
    /**
     * 提取的结构化数据 (JSON格式)
     */
    private String extractedData;
    
    /**
     * 用户类型 1-普通客户 2-中介操作员
     */
    private Integer userType;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 