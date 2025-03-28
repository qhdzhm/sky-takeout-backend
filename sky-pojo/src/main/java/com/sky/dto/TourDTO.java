package com.sky.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * 通用旅游数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourDTO {
    private Integer id;
    private String name;
    private String description;
    private BigDecimal price;
    // 折扣价格，应用代理商折扣后的价格
    private BigDecimal discountedPrice;
    private String duration;
    private String location;
    private Integer minPeople;
    private Integer maxPeople;
    private Integer currentBookings;
    private String coverImage;
    private String tourType; // "day" 或 "group"
    private Integer regionId;
    private String regionName;
    
    // 一日游特有字段
    private LocalTime departureTime;
    private LocalTime returnTime;
    
    // 跟团游特有字段
    private LocalDate startDate;
    private LocalDate endDate;
    
    // 额外信息
    private BigDecimal rating;
    private String departureAddress;
    private Integer guideId;
    private List<String> themes;
    private List<String> suitableFor;
    private List<String> highlights;
    private List<String> inclusions;
    private List<String> exclusions;
    private List<Map<String, Object>> itinerary;
    private List<Map<String, Object>> reviews;
    private List<String> images;
} 