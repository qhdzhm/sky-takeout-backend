package com.sky.service;

import com.sky.entity.AgentDiscountLevel;
import com.sky.entity.ProductAgentDiscount;
import com.sky.entity.AgentDiscountLog;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 增强版折扣服务接口
 * 支持A、B、C三档折扣，每个产品针对不同等级的不同折扣率
 */
public interface EnhancedDiscountService {

    // ===================== 折扣计算相关 =====================
    
    /**
     * 计算产品的代理商折扣价格（新版本，支持产品级别折扣）
     * @param productType 产品类型：day_tour, group_tour
     * @param productId 产品ID
     * @param originalPrice 原价
     * @param agentId 代理商ID
     * @param orderId 订单ID（可选，用于记录日志）
     * @return 折扣计算结果
     */
    Map<String, Object> calculateProductDiscount(String productType, Long productId, 
                                               BigDecimal originalPrice, Long agentId, Long orderId);

    /**
     * 获取代理商对特定产品的折扣率
     * @param agentId 代理商ID
     * @param productType 产品类型
     * @param productId 产品ID
     * @return 折扣率，如果没有配置则返回null
     */
    BigDecimal getAgentProductDiscountRate(Long agentId, String productType, Long productId);

    /**
     * 获取代理商的所有产品折扣配置
     * @param agentId 代理商ID
     * @return 折扣配置列表
     */
    List<ProductAgentDiscount> getAgentDiscountConfigs(Long agentId);

    // ===================== 折扣等级管理 =====================
    
    /**
     * 获取所有折扣等级
     * @return 折扣等级列表
     */
    List<AgentDiscountLevel> getAllDiscountLevels();

    /**
     * 获取活跃的折扣等级
     * @return 活跃的折扣等级列表
     */
    List<AgentDiscountLevel> getActiveDiscountLevels();

    /**
     * 根据ID获取折扣等级
     * @param id 等级ID
     * @return 折扣等级
     */
    AgentDiscountLevel getDiscountLevelById(Long id);

    /**
     * 根据等级代码获取折扣等级
     * @param levelCode 等级代码
     * @return 折扣等级
     */
    AgentDiscountLevel getDiscountLevelByCode(String levelCode);

    /**
     * 创建折扣等级
     * @param level 折扣等级信息
     * @return 创建的折扣等级
     */
    AgentDiscountLevel createDiscountLevel(AgentDiscountLevel level);

    /**
     * 更新折扣等级
     * @param level 折扣等级信息
     * @return 更新的折扣等级
     */
    AgentDiscountLevel updateDiscountLevel(AgentDiscountLevel level);

    /**
     * 删除折扣等级
     * @param id 等级ID
     * @return 是否删除成功
     */
    boolean deleteDiscountLevel(Long id);

    // ===================== 产品折扣配置管理 =====================
    
    /**
     * 获取产品的所有折扣配置
     * @param productType 产品类型
     * @param productId 产品ID
     * @return 折扣配置列表
     */
    List<ProductAgentDiscount> getProductDiscountConfigs(String productType, Long productId);

    /**
     * 根据等级ID获取所有折扣配置
     * @param levelId 等级ID
     * @return 折扣配置列表
     */
    List<ProductAgentDiscount> getDiscountConfigsByLevel(Long levelId);

    /**
     * 创建产品折扣配置
     * @param discount 折扣配置
     * @return 创建的折扣配置
     */
    ProductAgentDiscount createProductDiscount(ProductAgentDiscount discount);

    /**
     * 更新产品折扣配置
     * @param discount 折扣配置
     * @return 更新的折扣配置
     */
    ProductAgentDiscount updateProductDiscount(ProductAgentDiscount discount);

    /**
     * 删除产品折扣配置
     * @param id 配置ID
     * @return 是否删除成功
     */
    boolean deleteProductDiscount(Long id);

    /**
     * 批量更新某个等级的产品折扣率
     * @param levelId 等级ID
     * @param productType 产品类型（可选）
     * @param discountRate 新的折扣率
     * @return 更新的配置数量
     */
    int batchUpdateDiscountRate(Long levelId, String productType, BigDecimal discountRate);

    /**
     * 批量创建产品折扣配置
     * @param discounts 折扣配置列表
     * @return 创建的配置数量
     */
    int batchCreateProductDiscounts(List<ProductAgentDiscount> discounts);

    // ===================== 折扣日志管理 =====================
    
    /**
     * 查询代理商的折扣使用记录
     * @param agentId 代理商ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 折扣使用记录
     */
    List<AgentDiscountLog> getAgentDiscountLogs(Long agentId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询产品的折扣使用统计
     * @param productType 产品类型
     * @param productId 产品ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 折扣使用记录
     */
    List<AgentDiscountLog> getProductDiscountStats(String productType, Long productId, 
                                                  LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询折扣使用总体统计
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 折扣使用记录
     */
    List<AgentDiscountLog> getDiscountStats(LocalDateTime startTime, LocalDateTime endTime);
} 