package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统通知实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemNotification implements Serializable {

    private static final long serialVersionUID = 1L;

    // 通知ID
    private Long id;

    // 通知类型 1-新订单 2-聊天请求 3-订单修改 4-用户注册 5-退款申请 6-投诉建议 7-系统消息
    private Integer type;

    // 通知标题
    private String title;

    // 通知内容
    private String content;

    // 通知图标
    private String icon;

    // 相关数据ID（如订单ID、会话ID等）
    private Long relatedId;

    // 相关数据类型（order、session、user等）
    private String relatedType;

    // 通知级别 1-普通 2-重要 3-紧急
    private Integer level;

    // 是否已读 0-未读 1-已读
    private Integer isRead;

    // 接收者角色 1-管理员 2-客服 3-特定用户
    private Integer receiverRole;

    // 特定接收者ID（如果是发给特定用户）
    private Long receiverId;

    // 创建时间
    private LocalDateTime createTime;

    // 已读时间
    private LocalDateTime readTime;

    // 过期时间
    private LocalDateTime expireTime;
} 