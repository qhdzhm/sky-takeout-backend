package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 服务会话响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceSessionVO implements Serializable {

    private Long id;

    private String sessionNo;

    private Long userId;

    private String userName;

    private String userAvatar;

    private Long serviceId;

    private String serviceName;

    private Integer sessionStatus;

    private Integer sessionType;

    private String subject;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer waitDuration;

    private Integer serviceDuration;

    private LocalDateTime createTime;

    // 最新消息内容
    private String lastMessage;

    // 最新消息时间
    private LocalDateTime lastMessageTime;

    // 未读消息数量
    private Integer unreadCount;
} 