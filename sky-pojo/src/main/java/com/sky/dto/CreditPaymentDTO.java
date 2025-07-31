package com.sky.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 信用支付DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditPaymentDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long bookingId;
    
    private BigDecimal amount;
    
    private String note;
} 