package com.sky.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 支付分页查询DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentPageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // 页码
    private int page;

    // 每页记录数
    private int pageSize;

    // 支付方式
    private String paymentMethod;

    // 支付状态
    private String status;

    // 预订ID
    private Integer bookingId;
    
    // 交易号
    private String transactionId;
    
    // 最小金额
    private Double minAmount;
    
    // 最大金额
    private Double maxAmount;
    
    // 开始时间
    private LocalDateTime beginTime;
    
    // 结束时间
    private LocalDateTime endTime;
} 