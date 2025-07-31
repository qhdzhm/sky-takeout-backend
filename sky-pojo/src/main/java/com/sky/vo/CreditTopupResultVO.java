package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 信用额度充值结果VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditTopupResultVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long transactionId;
    
    private String transactionNo;
    
    private Long agentId;
    
    private BigDecimal amount;
    
    private String transactionType;
    
    private BigDecimal balanceBefore;
    
    private BigDecimal balanceAfter;
} 