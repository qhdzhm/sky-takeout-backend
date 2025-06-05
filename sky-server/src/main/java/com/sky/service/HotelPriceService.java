package com.sky.service;

import com.sky.entity.HotelPriceDifference;
import java.math.BigDecimal;
import java.util.List;

/**
 * 酒店价格服务接口
 */
public interface HotelPriceService {
    
    /**
     * 获取所有酒店价格差异
     * @return 酒店价格差异列表
     */
    List<HotelPriceDifference> getAllPriceDifferences();
    
    /**
     * 根据酒店星级获取价格差异
     * @param hotelLevel 酒店星级
     * @return 价格差异
     */
    BigDecimal getPriceDifferenceByLevel(String hotelLevel);
    
    /**
     * 获取基准酒店星级
     * @return 基准酒店星级
     */
    String getBaseHotelLevel();
    
    /**
     * 根据酒店星级获取单房差
     * @param hotelLevel 酒店星级
     * @return 单房差金额
     */
    BigDecimal getDailySingleRoomSupplementByLevel(String hotelLevel);
    
    /**
     * 根据酒店星级获取房间基础价格（双床房价格）
     * @param hotelLevel 酒店星级
     * @return 房间基础价格（每间每晚）
     */
    BigDecimal getHotelRoomPriceByLevel(String hotelLevel);
    
    /**
     * 根据酒店星级获取三床房价格
     * @param hotelLevel 酒店星级
     * @return 三床房价格（每间每晚）
     */
    BigDecimal getTripleBedRoomPriceByLevel(String hotelLevel);
    
    /**
     * 根据酒店星级获取三人房差价（三人房价格 - 双床房价格）
     * @param hotelLevel 酒店星级
     * @return 三人房差价
     */
    BigDecimal getTripleBedRoomPriceDifferenceByLevel(String hotelLevel);
} 