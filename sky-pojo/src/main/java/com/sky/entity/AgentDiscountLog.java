package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 中介折扣计算历史记录实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentDiscountLog implements Serializable {

    private static final long serialVersionUID = 1L;

    // 主键
    private Long id;
    
    // 中介ID
    private Long agentId;
    
    // 订单ID
    private Long orderId;
    
    // 产品类型
    private String productType;
    
    // 产品ID
    private Long productId;
    
    // 原价
    private BigDecimal originalPrice;
    
    // 实际使用的折扣率
    private BigDecimal discountRate;
    
    // 折扣金额
    private BigDecimal discountAmount;
    
    // 最终价格
    private BigDecimal finalPrice;
    
    // 使用的折扣等级
    private String levelCode;
    
    // 创建时间
    private LocalDateTime createdAt;
} 