package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 信用支付结果VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditPaymentResultVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long transactionId;
    private String transactionNo;
    private Long bookingId;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String paymentStatus;
} 