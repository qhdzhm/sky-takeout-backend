package com.sky.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 酒店价格差异实体类
 */
@Data
public class HotelPriceDifference {
    
    private Integer id;
    
    private String hotelLevel;
    
    private BigDecimal priceDifference;
    
    private Boolean isBaseLevel;
    
    private String description;
    
    /**
     * 单房差（每晚）
     */
    private BigDecimal dailySingleRoomSupplement;
    
    /**
     * 酒店房间基础价格（每间每晚）- 双床房价格
     */
    private BigDecimal hotelRoomPrice;
    
    /**
     * 三床房价格（每间每晚）
     */
    private BigDecimal tripleBedRoomPrice;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
} 