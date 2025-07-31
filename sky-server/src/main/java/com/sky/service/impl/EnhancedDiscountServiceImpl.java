package com.sky.service.impl;

import com.sky.entity.Agent;
import com.sky.entity.AgentDiscountLevel;
import com.sky.entity.AgentDiscountLog;
import com.sky.entity.ProductAgentDiscount;
import com.sky.mapper.AgentDiscountLevelMapper;
import com.sky.mapper.AgentDiscountLogMapper;
import com.sky.mapper.AgentMapper;
import com.sky.mapper.ProductAgentDiscountMapper;
import com.sky.service.EnhancedDiscountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 增强版折扣服务实现类
 */
@Service
@Slf4j
public class EnhancedDiscountServiceImpl implements EnhancedDiscountService {

    @Autowired
    private AgentMapper agentMapper;

    @Autowired
    private AgentDiscountLevelMapper agentDiscountLevelMapper;

    @Autowired
    private ProductAgentDiscountMapper productAgentDiscountMapper;

    @Autowired
    private AgentDiscountLogMapper agentDiscountLogMapper;

    // ===================== 折扣计算相关 =====================

    @Override
    public Map<String, Object> calculateProductDiscount(String productType, Long productId, 
                                                       BigDecimal originalPrice, Long agentId, Long orderId) {
        log.info("计算产品折扣价格 - 产品类型: {}, 产品ID: {}, 原价: {}, 代理商ID: {}, 订单ID: {}", 
                productType, productId, originalPrice, agentId, orderId);

        Map<String, Object> result = new HashMap<>();
        result.put("productType", productType);
        result.put("productId", productId);
        result.put("originalPrice", originalPrice);
        result.put("agentId", agentId);
        
        try {
            if (originalPrice == null || originalPrice.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("原价无效: {}", originalPrice);
                result.put("discountedPrice", BigDecimal.ZERO);
                result.put("discountRate", BigDecimal.ONE);
                result.put("savedAmount", BigDecimal.ZERO);
                result.put("levelCode", null);
                return result;
            }

            if (agentId == null) {
                log.info("代理商ID为空，不应用折扣");
                result.put("discountedPrice", originalPrice);
                result.put("discountRate", BigDecimal.ONE);
                result.put("savedAmount", BigDecimal.ZERO);
                result.put("levelCode", null);
                return result;
            }

            // 获取代理商对特定产品的折扣率
            BigDecimal discountRate = getAgentProductDiscountRate(agentId, productType, productId);
            
            if (discountRate == null) {
                log.info("未找到产品特定折扣配置，使用原价");
                result.put("discountedPrice", originalPrice);
                result.put("discountRate", BigDecimal.ONE);
                result.put("savedAmount", BigDecimal.ZERO);
                result.put("levelCode", null);
                return result;
            }

            // 计算折扣价格
            BigDecimal discountedPrice = originalPrice.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal savedAmount = originalPrice.subtract(discountedPrice).setScale(2, RoundingMode.HALF_UP);

            // 获取代理商等级信息
            Agent agent = agentMapper.getById(agentId);
            String levelCode = null;
            if (agent != null && agent.getDiscountLevelId() != null) {
                AgentDiscountLevel level = agentDiscountLevelMapper.findById(agent.getDiscountLevelId());
                if (level != null) {
                    levelCode = level.getLevelCode();
                }
            }

            result.put("discountedPrice", discountedPrice);
            result.put("discountRate", discountRate);
            result.put("savedAmount", savedAmount);
            result.put("levelCode", levelCode);

            // 记录折扣计算日志
            try {
                saveDiscountLog(agentId, orderId, productType, productId, originalPrice, 
                              discountRate, savedAmount, discountedPrice, levelCode);
            } catch (Exception e) {
                log.warn("保存折扣日志失败", e);
            }

            log.info("产品折扣计算完成 - 原价: {}, 折扣价: {}, 折扣率: {}, 节省: {}, 等级: {}", 
                    originalPrice, discountedPrice, discountRate, savedAmount, levelCode);

            return result;
        } catch (Exception e) {
            log.error("计算产品折扣价格异常", e);
            result.put("discountedPrice", originalPrice);
            result.put("discountRate", BigDecimal.ONE);
            result.put("savedAmount", BigDecimal.ZERO);
            result.put("levelCode", null);
            result.put("error", e.getMessage());
            return result;
        }
    }

    @Override
    // @Cacheable(value = "productDiscountRates", key = "#agentId + '_' + #productType + '_' + #productId") // 临时注释，避免Redis连接问题
    public BigDecimal getAgentProductDiscountRate(Long agentId, String productType, Long productId) {
        log.info("获取代理商产品折扣率 - 代理商ID: {}, 产品类型: {}, 产品ID: {}", agentId, productType, productId);
        
        try {
            ProductAgentDiscount discount = productAgentDiscountMapper.findByAgentAndProduct(agentId, productType, productId);
            if (discount != null) {
                log.info("找到产品特定折扣配置: {}", discount.getDiscountRate());
                return discount.getDiscountRate();
            }
            
            log.info("未找到产品特定折扣配置");
            return null;
        } catch (Exception e) {
            log.error("获取代理商产品折扣率异常", e);
            return null;
        }
    }

    @Override
    public List<ProductAgentDiscount> getAgentDiscountConfigs(Long agentId) {
        log.info("获取代理商所有折扣配置 - 代理商ID: {}", agentId);
        return productAgentDiscountMapper.findByAgentId(agentId);
    }

    /**
     * 保存折扣计算日志
     */
    private void saveDiscountLog(Long agentId, Long orderId, String productType, Long productId,
                               BigDecimal originalPrice, BigDecimal discountRate, BigDecimal discountAmount,
                               BigDecimal finalPrice, String levelCode) {
        try {
            AgentDiscountLog log = AgentDiscountLog.builder()
                    .agentId(agentId)
                    .orderId(orderId)
                    .productType(productType)
                    .productId(productId)
                    .originalPrice(originalPrice)
                    .discountRate(discountRate)
                    .discountAmount(discountAmount)
                    .finalPrice(finalPrice)
                    .levelCode(levelCode)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            agentDiscountLogMapper.insert(log);
            EnhancedDiscountServiceImpl.log.info("折扣日志保存成功");
        } catch (Exception e) {
            log.error("保存折扣日志失败", e);
        }
    }

    // ===================== 折扣等级管理 =====================

    @Override
    public List<AgentDiscountLevel> getAllDiscountLevels() {
        return agentDiscountLevelMapper.findAll();
    }

    @Override
    // @Cacheable(value = "activeDiscountLevels") // 临时注释，避免Redis连接问题
    public List<AgentDiscountLevel> getActiveDiscountLevels() {
        return agentDiscountLevelMapper.findActiveLevel();
    }

    @Override
    public AgentDiscountLevel getDiscountLevelById(Long id) {
        return agentDiscountLevelMapper.findById(id);
    }

    @Override
    // @Cacheable(value = "discountLevelByCode", key = "#levelCode") // 临时注释，避免Redis连接问题
    public AgentDiscountLevel getDiscountLevelByCode(String levelCode) {
        return agentDiscountLevelMapper.findByLevelCode(levelCode);
    }

    @Override
    @Transactional
    // @CacheEvict(value = {"activeDiscountLevels", "discountLevelByCode"}, allEntries = true) // 临时注释，避免Redis连接问题
    public AgentDiscountLevel createDiscountLevel(AgentDiscountLevel level) {
        level.setCreatedAt(LocalDateTime.now());
        level.setUpdatedAt(LocalDateTime.now());
        agentDiscountLevelMapper.insert(level);
        return level;
    }

    @Override
    @Transactional
    // @CacheEvict(value = {"activeDiscountLevels", "discountLevelByCode"}, allEntries = true) // 临时注释，避免Redis连接问题
    public AgentDiscountLevel updateDiscountLevel(AgentDiscountLevel level) {
        level.setUpdatedAt(LocalDateTime.now());
        agentDiscountLevelMapper.update(level);
        return level;
    }

    @Override
    @Transactional
    // @CacheEvict(value = {"activeDiscountLevels", "discountLevelByCode"}, allEntries = true) // 临时注释，避免Redis连接问题
    public boolean deleteDiscountLevel(Long id) {
        try {
            int result = agentDiscountLevelMapper.deleteById(id);
            return result > 0;
        } catch (Exception e) {
            log.error("删除折扣等级失败", e);
            return false;
        }
    }

    // ===================== 产品折扣配置管理 =====================

    @Override
    public List<ProductAgentDiscount> getProductDiscountConfigs(String productType, Long productId) {
        return productAgentDiscountMapper.findByProduct(productType, productId);
    }

    @Override
    public List<ProductAgentDiscount> getDiscountConfigsByLevel(Long levelId) {
        return productAgentDiscountMapper.findByLevelId(levelId);
    }

    @Override
    @Transactional
    // @CacheEvict(value = "productDiscountRates", allEntries = true) // 临时注释，避免Redis连接问题
    public ProductAgentDiscount createProductDiscount(ProductAgentDiscount discount) {
        discount.setCreatedAt(LocalDateTime.now());
        discount.setUpdatedAt(LocalDateTime.now());
        productAgentDiscountMapper.insert(discount);
        return discount;
    }

    @Override
    @Transactional
    // @CacheEvict(value = "productDiscountRates", allEntries = true) // 临时注释，避免Redis连接问题
    public ProductAgentDiscount updateProductDiscount(ProductAgentDiscount discount) {
        try {
            discount.setUpdatedAt(LocalDateTime.now());
            log.info("更新产品折扣配置: {}", discount);
            int result = productAgentDiscountMapper.update(discount);
            log.info("数据库更新结果: {}", result);
            if (result > 0) {
                log.info("产品折扣配置更新成功");
                return discount;
            } else {
                log.error("产品折扣配置更新失败，未影响任何行");
                throw new RuntimeException("更新失败，未影响任何行");
            }
        } catch (Exception e) {
            log.error("更新产品折扣配置失败", e);
            throw e;
        }
    }

    @Override
    @Transactional
    // @CacheEvict(value = "productDiscountRates", allEntries = true) // 临时注释，避免Redis连接问题
    public boolean deleteProductDiscount(Long id) {
        try {
            int result = productAgentDiscountMapper.deleteById(id);
            return result > 0;
        } catch (Exception e) {
            log.error("删除产品折扣配置失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    // @CacheEvict(value = "productDiscountRates", allEntries = true) // 临时注释，避免Redis连接问题
    public int batchUpdateDiscountRate(Long levelId, String productType, BigDecimal discountRate) {
        return productAgentDiscountMapper.batchUpdateDiscountRate(levelId, productType, discountRate);
    }

    @Override
    @Transactional
    // @CacheEvict(value = "productDiscountRates", allEntries = true) // 临时注释，避免Redis连接问题
    public int batchCreateProductDiscounts(List<ProductAgentDiscount> discounts) {
        LocalDateTime now = LocalDateTime.now();
        for (ProductAgentDiscount discount : discounts) {
            discount.setCreatedAt(now);
            discount.setUpdatedAt(now);
        }
        return productAgentDiscountMapper.batchInsert(discounts);
    }

    // ===================== 折扣日志管理 =====================

    @Override
    public List<AgentDiscountLog> getAgentDiscountLogs(Long agentId, LocalDateTime startTime, LocalDateTime endTime) {
        return agentDiscountLogMapper.findByAgentId(agentId, startTime, endTime);
    }

    @Override
    public List<AgentDiscountLog> getProductDiscountStats(String productType, Long productId, 
                                                         LocalDateTime startTime, LocalDateTime endTime) {
        return agentDiscountLogMapper.findByProduct(productType, productId, startTime, endTime);
    }

    @Override
    public List<AgentDiscountLog> getDiscountStats(LocalDateTime startTime, LocalDateTime endTime) {
        return agentDiscountLogMapper.findDiscountStats(startTime, endTime);
    }
} 