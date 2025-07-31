package com.sky.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 信用额度充值DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditTopupDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long agentId;
    
    private BigDecimal amount;
    
    private String note;
    
    private Long applicationId;
} 