package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 产品中介折扣配置实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductAgentDiscount implements Serializable {

    private static final long serialVersionUID = 1L;

    // 主键
    private Long id;
    
    // 产品类型：day_tour-一日游，group_tour-跟团游
    private String productType;
    
    // 产品ID
    private Long productId;
    
    // 折扣等级ID
    private Long levelId;
    
    // 折扣率：0.85表示85%即15%折扣
    private BigDecimal discountRate;
    
    // 最小订单金额限制
    private BigDecimal minOrderAmount;
    
    // 最大折扣金额限制
    private BigDecimal maxDiscountAmount;
    
    // 生效开始时间
    private LocalDateTime validFrom;
    
    // 生效结束时间，NULL表示永久有效
    private LocalDateTime validUntil;
    
    // 是否激活：1-激活，0-停用
    private Integer isActive;
    
    // 创建时间
    private LocalDateTime createdAt;
    
    // 更新时间
    private LocalDateTime updatedAt;
} 