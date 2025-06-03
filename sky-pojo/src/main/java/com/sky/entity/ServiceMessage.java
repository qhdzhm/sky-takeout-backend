package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 服务消息实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    // 消息ID
    private Long id;

    // 会话ID
    private Long sessionId;

    // 发送者ID
    private Long senderId;

    // 接收者ID
    private Long receiverId;

    // 消息类型 1-文本 2-图片 3-文件 4-系统消息 5-AI消息
    private Integer messageType;

    // 发送者类型 1-用户 2-客服 3-系统 4-AI
    private Integer senderType;

    // 消息内容
    private String content;

    // 媒体文件URL（图片、文件等）
    private String mediaUrl;

    // 消息状态 0-发送中 1-已发送 2-已送达 3-已读
    private Integer messageStatus;

    // 是否为AI转人工的消息
    private Boolean isFromAi;

    // AI消息相关的对话上下文ID
    private String aiContextId;

    // 创建时间
    private LocalDateTime createTime;

    // 发送时间
    private LocalDateTime sendTime;

    // 送达时间
    private LocalDateTime deliverTime;

    // 已读时间
    private LocalDateTime readTime;
} 