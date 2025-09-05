package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付审计日志实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentAuditLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 操作类型，如credit_payment/refund
     */
    private String action;

    /**
     * 订单ID
     */
    private Integer bookingId;

    /**
     * 订单号
     */
    private String orderNumber;

    /**
     * 代理商ID
     */
    private Long agentId;

    /**
     * 操作员ID
     */
    private Long operatorId;

    /**
     * 操作员类型
     */
    private String operatorType;

    /**
     * 操作员姓名
     */
    private String operatorName;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 操作前余额
     */
    private BigDecimal balanceBefore;

    /**
     * 操作后余额
     */
    private BigDecimal balanceAfter;

    /**
     * 备注
     */
    private String note;

    /**
     * IP地址
     */
    private String ip;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}