package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 信用额度申请VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditApplicationVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    
    private String applicationNo;
    
    private Long agentId;
    
    private String agentName;
    
    private String contactPerson;
    
    private String contactPhone;
    
    private BigDecimal requestedAmount;
    
    private BigDecimal approvedAmount;
    
    private String status;
    
    private String reason;
    
    private String adminComment;
    
    private Long adminId;
    
    private String adminName;
    
    private LocalDateTime submittedAt;
    
    private LocalDateTime processedAt;
} 