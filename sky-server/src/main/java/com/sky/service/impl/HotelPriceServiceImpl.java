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
    public BigDecimal getHotelRoomPriceByLevel(String hotelLevel) {
        if (hotelLevel == null || hotelLevel.trim().isEmpty()) {
            // 如果未提供酒店等级，返回基准等级的房间价格（0）
            return BigDecimal.ZERO;
        }
        
        // 处理3.5星映射为3星
        String mappedLevel = mapHotelLevel(hotelLevel);
        HotelPriceDifference hotelPrice = hotelPriceDifferenceMapper.selectByLevel(mappedLevel);
        
        if (hotelPrice == null) {
            log.warn("未找到酒店等级{}的房间价格信息，使用默认值0", mappedLevel);
            return BigDecimal.ZERO;
        }
        
        return hotelPrice.getHotelRoomPrice() != null ? 
               hotelPrice.getHotelRoomPrice() : BigDecimal.ZERO;
    }
    
    @Override
    public BigDecimal getTripleBedRoomPriceByLevel(String hotelLevel) {
        if (hotelLevel == null || hotelLevel.trim().isEmpty()) {
            // 如果未提供酒店等级，返回基准等级的三床房价格（0）
            return BigDecimal.ZERO;
        }
        
        // 处理3.5星映射为3星
        String mappedLevel = mapHotelLevel(hotelLevel);
        HotelPriceDifference hotelPrice = hotelPriceDifferenceMapper.selectByLevel(mappedLevel);
        
        if (hotelPrice == null) {
            log.warn("未找到酒店等级{}的三床房价格信息，使用默认值0", mappedLevel);
            return BigDecimal.ZERO;
        }
        
        return hotelPrice.getTripleBedRoomPrice() != null ? 
               hotelPrice.getTripleBedRoomPrice() : BigDecimal.ZERO;
    }
    
    @Override
    public BigDecimal getTripleBedRoomPriceDifferenceByLevel(String hotelLevel) {
        if (hotelLevel == null || hotelLevel.trim().isEmpty()) {
            // 如果未提供酒店等级，返回默认差价0
            return BigDecimal.ZERO;
        }
        
        // 处理3.5星映射为3星
        String mappedLevel = mapHotelLevel(hotelLevel);
        HotelPriceDifference hotelPrice = hotelPriceDifferenceMapper.selectByLevel(mappedLevel);
        
        if (hotelPrice == null) {
            log.warn("未找到酒店等级{}的价格信息，三人房差价使用默认值0", mappedLevel);
            return BigDecimal.ZERO;
        }
        
        // 获取三人房价格和双床房价格
        BigDecimal tripleBedPrice = hotelPrice.getTripleBedRoomPrice() != null ? 
                                    hotelPrice.getTripleBedRoomPrice() : BigDecimal.ZERO;
        BigDecimal doubleBedPrice = hotelPrice.getHotelRoomPrice() != null ? 
                                    hotelPrice.getHotelRoomPrice() : BigDecimal.ZERO;
        
        // 计算差价
        BigDecimal difference = tripleBedPrice.subtract(doubleBedPrice);
        
        log.info("酒店等级{}三人房差价计算: 三人房价格{}（元/晚） - 双床房价格{}（元/晚） = {}（元/晚）", 
                 mappedLevel, tripleBedPrice, doubleBedPrice, difference);
        
        return difference;
    }
} 