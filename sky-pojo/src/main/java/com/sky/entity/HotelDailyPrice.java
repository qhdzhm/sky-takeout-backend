package com.sky.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 酒店每日价格实体类
 */
@Data
public class HotelDailyPrice {
    
    private Long id;
    
    /**
     * 酒店星级
     */
    private String hotelLevel;
    
    /**
     * 价格日期
     */
    private LocalDate priceDate;
    
    /**
     * 价格差异（元/人）- 相对于基准星级
     */
    private BigDecimal priceDifference;
    
    /**
     * 单房差（元/晚）
     */
    private BigDecimal dailySingleRoomSupplement;
    
    /**
     * 备注信息
     */
    private String notes;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}







