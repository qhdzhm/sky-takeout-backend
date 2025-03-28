package com.sky.service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 折扣服务接口
 */
public interface DiscountService {

    /**
     * 获取代理商折扣率
     * @param agentId 代理商ID
     * @return 折扣率
     */
    BigDecimal getAgentDiscountRate(Long agentId);
    
    /**
     * 获取折扣价格
     * @param originalPrice 原价
     * @param agentId 代理商ID
     * @return 折扣价格
     */
    BigDecimal getDiscountedPrice(BigDecimal originalPrice, Long agentId);
    
    /**
     * 计算旅游产品折扣价格
     * @param tourId 旅游产品ID
     * @param tourType 旅游产品类型
     * @param originalPrice 原价
     * @param agentId 代理商ID
     * @return 折扣价格信息Map
     */
    Map<String, Object> calculateTourDiscount(Long tourId, String tourType, BigDecimal originalPrice, Long agentId);
    
    /**
     * 保存折扣计算历史
     * @param tourId 旅游产品ID
     * @param tourType 旅游产品类型
     * @param agentId 代理商ID
     * @param originalPrice 原价
     * @param discountedPrice 折扣价
     * @param discountRate 折扣率
     */
    void saveDiscountHistory(Long tourId, String tourType, Long agentId, BigDecimal originalPrice, BigDecimal discountedPrice, BigDecimal discountRate);
} 