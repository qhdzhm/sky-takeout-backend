package com.sky.service.impl;

import com.sky.entity.HotelPriceDifference;
import com.sky.mapper.HotelPriceDifferenceMapper;
import com.sky.service.HotelPriceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 酒店价格服务实现类
 */
@Service
@Slf4j
public class HotelPriceServiceImpl implements HotelPriceService {
    
    @Autowired
    private HotelPriceDifferenceMapper hotelPriceDifferenceMapper;
    
    /**
     * 处理特殊星级映射，3.5星使用3星配置
     */
    private String mapHotelLevel(String hotelLevel) {
        if ("3.5星".equals(hotelLevel)) {
            log.info("3.5星酒店使用3星价格配置");
            return "3星";
        }
        return hotelLevel;
    }
    
    @Override
    public List<HotelPriceDifference> getAllPriceDifferences() {
        return hotelPriceDifferenceMapper.selectAll();
    }
    
    @Override
    public BigDecimal getPriceDifferenceByLevel(String hotelLevel) {
        if (hotelLevel == null || hotelLevel.trim().isEmpty()) {
            // 如果未提供酒店等级，返回基准等级的价格差异（0）
            return BigDecimal.ZERO;
        }
        
        // 处理3.5星映射为3星
        String mappedLevel = mapHotelLevel(hotelLevel);
        HotelPriceDifference priceDiff = hotelPriceDifferenceMapper.selectByLevel(mappedLevel);
        
        if (priceDiff == null) {
            log.warn("未找到酒店等级{}的价格差异信息，使用基准价格", mappedLevel);
            return BigDecimal.ZERO;
        }
        
        return priceDiff.getPriceDifference();
    }
    
    @Override
    public String getBaseHotelLevel() {
        HotelPriceDifference baseLevel = hotelPriceDifferenceMapper.selectBaseLevel();
        return baseLevel != null ? baseLevel.getHotelLevel() : "4星"; // 默认返回4星
    }
    
    @Override
    public BigDecimal getDailySingleRoomSupplementByLevel(String hotelLevel) {
        if (hotelLevel == null || hotelLevel.trim().isEmpty()) {
            // 如果未提供酒店等级，返回基准等级的单房差（0）
            return BigDecimal.ZERO;
        }
        
        // 处理3.5星映射为3星
        String mappedLevel = mapHotelLevel(hotelLevel);
        HotelPriceDifference hotelPrice = hotelPriceDifferenceMapper.selectByLevel(mappedLevel);
        
        if (hotelPrice == null) {
            log.warn("未找到酒店等级{}的单房差信息，使用默认值0", mappedLevel);
            return BigDecimal.ZERO;
        }
        
        return hotelPrice.getDailySingleRoomSupplement() != null ? 
               hotelPrice.getDailySingleRoomSupplement() : BigDecimal.ZERO;
    }
    
    @Override
    public BigDecimal calculateRoomPriceByTypeAndLevel(String hotelLevel, String roomType) {
        BigDecimal singleRoomSupplement = getDailySingleRoomSupplementByLevel(hotelLevel);
        
        if (singleRoomSupplement.compareTo(BigDecimal.ZERO) == 0) {
            log.warn("单房差为0，房间价格计算返回0");
            return BigDecimal.ZERO;
        }
        
        // 根据房型确定乘数
        int multiplier = 2; // 默认双人间
        
        if (roomType != null) {
            String lowerRoomType = roomType.toLowerCase();
            // 三人间相关的房型
            if (lowerRoomType.contains("三人间") || lowerRoomType.contains("三床") || 
                lowerRoomType.contains("家庭") || lowerRoomType.contains("triple") || 
                lowerRoomType.contains("family")) {
                multiplier = 3;
                log.info("识别为三人间，使用乘数3: {}", roomType);
            }
            // 单人间相关的房型
            else if (lowerRoomType.contains("单人间") || lowerRoomType.contains("单床") || 
                     lowerRoomType.contains("single")) {
                multiplier = 1;
                log.info("识别为单人间，使用乘数1: {}", roomType);
            }
            // 双人间相关的房型（默认）
            else {
                log.info("识别为双人间，使用乘数2: {}", roomType);
            }
        }
        
        BigDecimal roomPrice = singleRoomSupplement.multiply(BigDecimal.valueOf(multiplier));
        log.info("房间价格计算: {}星酒店, 房型={}, 单房差={}, 乘数={}, 房价={}", 
                 hotelLevel, roomType, singleRoomSupplement, multiplier, roomPrice);
        
        return roomPrice;
    }
    
    @Override
    public BigDecimal getDoubleRoomPriceByLevel(String hotelLevel) {
        BigDecimal singleRoomSupplement = getDailySingleRoomSupplementByLevel(hotelLevel);
        BigDecimal doubleRoomPrice = singleRoomSupplement.multiply(BigDecimal.valueOf(2));
        
        log.info("双床房价格计算: {}星酒店, 单房差={}, 双床房价格={} (单房差×2)", 
                 hotelLevel, singleRoomSupplement, doubleRoomPrice);
        
        return doubleRoomPrice;
    }
    
    @Override
    public BigDecimal getTripleRoomPriceByLevel(String hotelLevel) {
        BigDecimal singleRoomSupplement = getDailySingleRoomSupplementByLevel(hotelLevel);
        BigDecimal tripleRoomPrice = singleRoomSupplement.multiply(BigDecimal.valueOf(3));
        
        log.info("三人房价格计算: {}星酒店, 单房差={}, 三人房价格={} (单房差×3)", 
                 hotelLevel, singleRoomSupplement, tripleRoomPrice);
        
        return tripleRoomPrice;
    }
    
    @Override
    public BigDecimal getTripleBedRoomPriceDifferenceByLevel(String hotelLevel) {
        // 三人房差价 = 三人房价格 - 双床房价格
        // = (单房差 × 3) - (单房差 × 2)
        // = 单房差 × 1
        BigDecimal singleRoomSupplement = getDailySingleRoomSupplementByLevel(hotelLevel);
        
        log.info("酒店等级{}三人房差价: {} (单房差×1)", hotelLevel, singleRoomSupplement);
        
        return singleRoomSupplement;
    }
    
    @Override
    public HotelPriceDifference getById(Integer id) {
        return hotelPriceDifferenceMapper.selectById(id);
    }
    
    @Override
    public boolean add(HotelPriceDifference hotelPriceDifference) {
        int result = hotelPriceDifferenceMapper.insert(hotelPriceDifference);
        return result > 0;
    }
    
    @Override
    public boolean update(HotelPriceDifference hotelPriceDifference) {
        int result = hotelPriceDifferenceMapper.update(hotelPriceDifference);
        return result > 0;
    }
    
    @Override
    public boolean delete(Integer id) {
        int result = hotelPriceDifferenceMapper.deleteById(id);
        return result > 0;
    }
} 