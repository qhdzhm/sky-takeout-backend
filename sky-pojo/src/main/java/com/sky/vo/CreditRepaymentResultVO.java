package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 信用额度还款结果数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditRepaymentResultVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 交易ID
     */
    private Long transactionId;
    
    /**
     * 交易编号
     */
    private String transactionNo;
    
    /**
     * 还款金额
     */
    private BigDecimal amount;
    
    /**
     * 还款后的可用余额
     */
    private BigDecimal balanceAfter;
    
    /**
     * 还款状态
     */
    private String repaymentStatus;
} 