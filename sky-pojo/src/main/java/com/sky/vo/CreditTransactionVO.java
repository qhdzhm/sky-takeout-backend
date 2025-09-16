package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 信用交易VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditTransactionVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * ID
     */
    private Long id;
    
    /**
     * 交易编号
     */
    private String transactionNo;
    
    /**
     * 代理商ID
     */
    private Long agentId;
    
    /**
     * 代理商名称（数据库中不存储）
     */
    private String agentName;
    
    /**
     * 订单ID（如适用）
     */
    private Long bookingId;
    
    /**
     * 订单号（如适用）
     */
    private String orderNumber;
    
    /**
     * 交易类型：CREDIT_GRANTED(授信), CREDIT_USED(使用), CREDIT_REPAID(还款), CREDIT_TOPUP(充值)
     */
    private String transactionType;
    
    /**
     * 交易金额
     */
    private BigDecimal amount;
    
    /**
     * 备注
     */
    private String description;
    
    /**
     * 交易前余额
     */
    private BigDecimal balanceBefore;
    
    /**
     * 交易后余额
     */
    private BigDecimal balanceAfter;
    
    /**
     * 创建人ID
     */
    private Long createdBy;
    
    /**
     * 创建人姓名
     */
    private String createdByName;
    
    /**
     * 创建时间（用于前端显示）
     */
    private LocalDateTime createdAt;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 