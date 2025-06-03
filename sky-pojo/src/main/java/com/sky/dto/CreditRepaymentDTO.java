package com.sky.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 信用额度还款数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditRepaymentDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 还款金额
     */
    private BigDecimal amount;
    
    /**
     * 还款备注
     */
    private String note;
} 