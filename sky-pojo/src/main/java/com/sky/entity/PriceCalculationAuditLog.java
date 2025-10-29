package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 价格计算审计日志
 * 记录每次价格计算请求的详细信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceCalculationAuditLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    // ==================== 请求信息 ====================
    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 代理商ID
     */
    private Integer agentId;

    /**
     * 用户类型（agent/user/guest）
     */
    private String userType;

    /**
     * 会话ID
     */
    private String sessionId;

    // ==================== 产品信息 ====================
    /**
     * 产品ID
     */
    private Integer tourId;

    /**
     * 产品类型（day_tour/group_tour）
     */
    private String tourType;

    // ==================== 输入参数 ====================
    /**
     * 输入参数（JSON格式）
     */
    private String inputParams;

    // ==================== 计算结果 ====================
    /**
     * 计算出的价格
     */
    private BigDecimal calculatedPrice;

    /**
     * 计算明细（JSON格式）
     */
    private String calculationDetails;

    /**
     * 计算耗时（毫秒）
     */
    private Integer calculationDurationMs;

    // ==================== 请求来源 ====================
    /**
     * 客户端IP地址
     */
    private String ipAddress;

    /**
     * 用户代理（浏览器信息）
     */
    private String userAgent;

    /**
     * 请求URL
     */
    private String requestUrl;

    /**
     * 来源页面
     */
    private String referer;

    // ==================== 状态和错误 ====================
    /**
     * 状态（success/error/validation_failed）
     */
    private String status;

    /**
     * 错误信息
     */
    private String errorMessage;

    // ==================== 异常标记 ====================
    /**
     * 是否可疑（异常价格、频繁请求等）
     */
    private Boolean isSuspicious;

    /**
     * 可疑原因
     */
    private String suspiciousReason;

    // ==================== 时间戳 ====================
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}







