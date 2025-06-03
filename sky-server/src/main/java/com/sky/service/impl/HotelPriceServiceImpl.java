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
        
        HotelPriceDifference priceDiff = hotelPriceDifferenceMapper.selectByLevel(hotelLevel);
        
        if (priceDiff == null) {
            log.warn("未找到酒店等级{}的价格差异信息，使用基准价格", hotelLevel);
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
        
        HotelPriceDifference hotelPrice = hotelPriceDifferenceMapper.selectByLevel(hotelLevel);
        
        if (hotelPrice == null) {
            log.warn("未找到酒店等级{}的单房差信息，使用默认值0", hotelLevel);
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
        
        HotelPriceDifference hotelPrice = hotelPriceDifferenceMapper.selectByLevel(hotelLevel);
        
        if (hotelPrice == null) {
            log.warn("未找到酒店等级{}的房间价格信息，使用默认值0", hotelLevel);
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
        
        HotelPriceDifference hotelPrice = hotelPriceDifferenceMapper.selectByLevel(hotelLevel);
        
        if (hotelPrice == null) {
            log.warn("未找到酒店等级{}的三床房价格信息，使用默认值0", hotelLevel);
            return BigDecimal.ZERO;
        }
        
        return hotelPrice.getTripleBedRoomPrice() != null ? 
               hotelPrice.getTripleBedRoomPrice() : BigDecimal.ZERO;
    }
} 