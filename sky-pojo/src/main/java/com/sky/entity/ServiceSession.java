package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 服务会话实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceSession implements Serializable {

    private static final long serialVersionUID = 1L;

    // 会话ID
    private Long id;

    // 会话编号
    private String sessionNo;

    // 用户ID
    private Long userId;

    // 用户类型 1-普通用户(users) 2-代理商(agents) 3-代理商操作员(agent_operators)
    private Integer userType;

    // 客服ID
    private Long employeeId;

    // 会话状态 0-等待分配 1-服务中 2-用户结束 3-客服结束 4-系统超时结束
    private Integer sessionStatus;

    // 会话类型 1-主动咨询 2-AI转人工 3-投诉建议
    private Integer sessionType;

    // 会话主题/问题描述
    private String subject;

    // 用户满意度评分 1-5分
    private Integer userRating;

    // 用户评价内容
    private String userComment;

    // 客服备注
    private String serviceRemark;

    // 会话开始时间
    private LocalDateTime startTime;

    // 会话结束时间
    private LocalDateTime endTime;

    // 等待时长（秒）
    private Integer waitDuration;

    // 服务时长（秒）
    private Integer serviceDuration;

    // 创建时间
    private LocalDateTime createTime;

    // 更新时间
    private LocalDateTime updateTime;
} 