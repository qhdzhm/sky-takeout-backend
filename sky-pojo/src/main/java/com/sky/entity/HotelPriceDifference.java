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
     * 注：双床房价格 = dailySingleRoomSupplement × 2
     *     三人房价格 = dailySingleRoomSupplement × 3
     */
    private BigDecimal dailySingleRoomSupplement;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
} 