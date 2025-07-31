package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 代理商信用额度信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentCredit implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 代理商ID
     */
    private Long agentId;
    
    /**
     * 总额度
     */
    private BigDecimal totalCredit;
    
    /**
     * 已使用额度
     */
    private BigDecimal usedCredit;
    
    /**
     * 可用额度
     */
    private BigDecimal availableCredit;
    
    /**
     * 预存余额
     */
    private BigDecimal depositBalance;
    
    /**
     * 信用评级
     */
    private String creditRating;
    
    /**
     * 信用利率(%)
     */
    private BigDecimal interestRate;
    
    /**
     * 账单周期日
     */
    private Integer billingCycleDay;
    
    /**
     * 最后结算日期
     */
    private LocalDate lastSettlementDate;
    
    /**
     * 透支次数
     */
    private Integer overdraftCount;
    
    /**
     * 是否冻结:0-正常,1-冻结
     */
    private Boolean isFrozen;
    
    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdated;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
} 