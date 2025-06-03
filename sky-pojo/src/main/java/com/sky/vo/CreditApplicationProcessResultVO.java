package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 信用额度申请处理结果VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditApplicationProcessResultVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long applicationId;
    private String status;
    private BigDecimal approvedAmount;
} 