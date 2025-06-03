package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 价格详情VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceDetailVO {
    /**
     * 总价（显示给用户的价格）
     */
    private BigDecimal totalPrice;
    
    /**
     * 基础价格（不含额外房费）
     */
    private BigDecimal basePrice;
    
    /**
     * 额外房费
     */
    private BigDecimal extraRoomFee;
    
    /**
     * 非代理商价格（普通用户价格）
     */
    private BigDecimal nonAgentPrice;
    
    /**
     * 实际支付价格（内部计算用，操作员看不到）
     */
    private BigDecimal actualPaymentPrice;
    
    /**
     * 折扣率
     */
    private BigDecimal discountRate;
    
    /**
     * 是否显示折扣信息
     */
    private Boolean showDiscount;
    
    /**
     * 原价（未打折前的价格）
     */
    private BigDecimal originalPrice;
} 