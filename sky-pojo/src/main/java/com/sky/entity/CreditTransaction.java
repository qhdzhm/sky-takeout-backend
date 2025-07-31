package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 信用交易记录
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditTransaction implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String transactionNo;
    private Long agentId;
    private BigDecimal amount;
    private String transactionType;
    private Long bookingId;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String note;
    private Long createdBy;
    private LocalDateTime createdAt;
} 