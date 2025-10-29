package com.sky.service;

import com.sky.entity.HotelDailyPrice;

import java.time.LocalDate;
import java.util.List;

/**
 * 酒店每日价格服务接口
 */
public interface HotelDailyPriceService {
    
    /**
     * 根据酒店星级和日期范围查询每日价格
     */
    List<HotelDailyPrice> getByLevelAndDateRange(String hotelLevel, LocalDate startDate, LocalDate endDate);
    
    /**
     * 根据日期范围查询所有星级的每日价格
     */
    List<HotelDailyPrice> getByDateRange(LocalDate startDate, LocalDate endDate);
    
    /**
     * 根据酒店星级和具体日期查询价格
     */
    HotelDailyPrice getByLevelAndDate(String hotelLevel, LocalDate priceDate);
    
    /**
     * 添加或更新每日价格
     */
    boolean saveOrUpdate(HotelDailyPrice hotelDailyPrice);
    
    /**
     * 批量添加或更新每日价格
     */
    boolean batchSaveOrUpdate(List<HotelDailyPrice> list);
    
    /**
     * 删除每日价格
     */
    boolean delete(Long id);
    
    /**
     * 根据星级和日期删除
     */
    boolean deleteByLevelAndDate(String hotelLevel, LocalDate priceDate);
    
    /**
     * 根据酒店星级和日期获取价格差异
     * 如果没有设置，返回默认值 0
     */
    java.math.BigDecimal getPriceDifferenceByLevelAndDate(String hotelLevel, LocalDate priceDate);
    
    /**
     * 根据酒店星级和日期获取单房差
     * 如果没有设置，返回默认值 0
     */
    java.math.BigDecimal getSingleRoomSupplementByLevelAndDate(String hotelLevel, LocalDate priceDate);
}

