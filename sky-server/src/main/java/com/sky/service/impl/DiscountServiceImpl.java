package com.sky.service.impl;

import com.sky.entity.Agent;
import com.sky.mapper.AgentMapper;
import com.sky.service.DiscountService;
import com.sky.service.EnhancedDiscountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 折扣服务实现类
 */
@Service
@Slf4j
public class DiscountServiceImpl implements DiscountService {

    @Autowired
    private AgentMapper agentMapper;
    
    @Autowired
    private EnhancedDiscountService enhancedDiscountService;

    /**
     * 获取代理商折扣率
     * @param agentId 代理商ID
     * @return 折扣率
     */
    @Override
    @Cacheable(value = "discountRates", key = "#agentId", unless = "#result == null")
    public BigDecimal getAgentDiscountRate(Long agentId) {
        log.info("获取代理商折扣率，代理商ID: {}", agentId);
        
        if (agentId == null) {
            log.warn("代理商ID为空，返回无折扣");
            return BigDecimal.ONE;
        }
        
        try {
            Agent agent = agentMapper.getById(agentId);
            if (agent == null) {
                log.warn("未找到代理商信息，ID: {}", agentId);
                return BigDecimal.ONE;
            }
            
            BigDecimal discountRate = agent.getDiscountRate();
            if (discountRate == null) {
                log.warn("代理商折扣率为空，使用默认值0.9，代理商ID: {}", agentId);
                return new BigDecimal("0.9");
            }
            
            log.info("成功获取代理商折扣率: {}, 代理商ID: {}", discountRate, agentId);
            return discountRate;
        } catch (Exception e) {
            log.error("获取代理商折扣率异常", e);
            return BigDecimal.ONE;
        }
    }

    /**
     * 获取折扣价格
     * @param originalPrice 原价
     * @param agentId 代理商ID
     * @return 折扣价格
     */
    @Override
    public BigDecimal getDiscountedPrice(BigDecimal originalPrice, Long agentId) {
        log.info("计算折扣价格，原价: {}, 代理商ID: {}", originalPrice, agentId);
        
        if (originalPrice == null) {
            log.warn("原价为空，返回0");
            return BigDecimal.ZERO;
        }
        
        if (agentId == null) {
            log.info("代理商ID为空，返回原价");
            return originalPrice;
        }
        
        try {
            BigDecimal discountRate = getAgentDiscountRate(agentId);
            BigDecimal discountedPrice = originalPrice.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
            
            log.info("折扣价格计算结果: 原价 {} * 折扣率 {} = 折扣价 {}", originalPrice, discountRate, discountedPrice);
            return discountedPrice;
        } catch (Exception e) {
            log.error("计算折扣价格异常", e);
            return originalPrice;
        }
    }

    /**
     * 计算旅游产品折扣价格（升级版：优先使用产品级别折扣，兼容原有统一折扣）
     * @param tourId 旅游产品ID
     * @param tourType 旅游产品类型
     * @param originalPrice 原价
     * @param agentId 代理商ID
     * @return 折扣价格信息Map
     */
    @Override
    public Map<String, Object> calculateTourDiscount(Long tourId, String tourType, BigDecimal originalPrice, Long agentId) {
        log.info("计算旅游产品折扣价格，旅游ID: {}, 类型: {}, 原价: {}, 代理商ID: {}", 
                tourId, tourType, originalPrice, agentId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("tourId", tourId);
        result.put("tourType", tourType);
        result.put("originalPrice", originalPrice);
        
        try {
            if (originalPrice == null) {
                log.warn("原价为空，使用默认值0");
                result.put("originalPrice", BigDecimal.ZERO);
                result.put("discountedPrice", BigDecimal.ZERO);
                result.put("discountRate", BigDecimal.ONE);
                result.put("savedAmount", BigDecimal.ZERO);
                return result;
            }
            
            if (agentId == null) {
                log.info("代理商ID为空，不应用折扣");
                result.put("discountedPrice", originalPrice);
                result.put("discountRate", BigDecimal.ONE);
                result.put("savedAmount", BigDecimal.ZERO);
                return result;
            }
            
            // 尝试使用新的产品级别折扣系统
            try {
                BigDecimal productDiscountRate = enhancedDiscountService.getAgentProductDiscountRate(agentId, tourType, tourId);
                if (productDiscountRate != null) {
                    log.info("使用产品级别折扣率: {}", productDiscountRate);
                    
                    // 使用新的折扣计算方式
                    Map<String, Object> enhancedResult = enhancedDiscountService.calculateProductDiscount(
                            tourType, tourId, originalPrice, agentId, null);
                    
                    // 将新结果映射到原有结果格式以保持兼容性
                    result.put("discountedPrice", enhancedResult.get("discountedPrice"));
                    result.put("discountRate", enhancedResult.get("discountRate"));
                    result.put("savedAmount", enhancedResult.get("savedAmount"));
                    result.put("levelCode", enhancedResult.get("levelCode"));
                    result.put("enhancedMode", true); // 标记使用了增强模式
                    
                    log.info("使用产品级别折扣计算完成");
                    return result;
                }
            } catch (Exception e) {
                log.warn("产品级别折扣计算失败，回退到统一折扣模式", e);
            }
            
            // 回退到原有的统一折扣率逻辑
            log.info("使用传统统一折扣率模式");
            
            // 获取代理商折扣率
            BigDecimal discountRate = getAgentDiscountRate(agentId);
            
            // 计算折扣价格
            BigDecimal discountedPrice = originalPrice.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
            
            // 计算节省金额
            BigDecimal savedAmount = originalPrice.subtract(discountedPrice).setScale(2, RoundingMode.HALF_UP);
            
            result.put("discountedPrice", discountedPrice);
            result.put("discountRate", discountRate);
            result.put("savedAmount", savedAmount);
            result.put("enhancedMode", false); // 标记使用了传统模式
            
            // 保存折扣计算历史
            try {
                saveDiscountHistory(tourId, tourType, agentId, originalPrice, discountedPrice, discountRate);
            } catch (Exception e) {
                log.warn("保存折扣计算历史失败", e);
            }
            
            log.info("旅游产品折扣计算完成 - ID: {}, 类型: {}, 原价: {}, 折扣价: {}, 折扣率: {}, 节省: {}", 
                    tourId, tourType, originalPrice, discountedPrice, discountRate, savedAmount);
            
            return result;
        } catch (Exception e) {
            log.error("计算旅游产品折扣价格异常", e);
            result.put("discountedPrice", originalPrice);
            result.put("discountRate", BigDecimal.ONE);
            result.put("savedAmount", BigDecimal.ZERO);
            result.put("error", e.getMessage());
            return result;
        }
    }

    /**
     * 保存折扣计算历史
     * @param tourId 旅游产品ID
     * @param tourType 旅游产品类型
     * @param agentId 代理商ID
     * @param originalPrice 原价
     * @param discountedPrice 折扣价
     * @param discountRate 折扣率
     */
    @Override
    public void saveDiscountHistory(Long tourId, String tourType, Long agentId, BigDecimal originalPrice, 
                                  BigDecimal discountedPrice, BigDecimal discountRate) {
        // 此处可以实现将折扣计算历史保存到数据库的逻辑
        // 这里仅记录日志，实际项目中应该将数据保存到数据库
        log.info("记录折扣计算历史 - 时间: {}, 旅游ID: {}, 类型: {}, 代理商ID: {}, 原价: {}, 折扣价: {}, 折扣率: {}", 
                LocalDateTime.now(), tourId, tourType, agentId, originalPrice, discountedPrice, discountRate);
    }
} 