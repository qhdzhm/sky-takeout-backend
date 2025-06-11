package com.sky.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 跟团游数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupTourDTO {
    private Integer id;
    private String name;
    private String description;
    private BigDecimal price;
    // 折扣价格，应用代理商折扣后的价格
    private BigDecimal discountedPrice;
    /** 时长（天） */
    private String duration;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer minPeople;
    private Integer maxPeople;
    private Integer currentBookings;
    private String coverImage;
    private String bannerImage;
    private String productShowcaseImage;
    private Integer regionId;
    private String regionName;
    
    // 额外信息
    private BigDecimal rating;
    private String departureAddress;
    private Integer guideId;
    private List<String> themes;
    private List<Integer> themeIds; // 主题ID列表，用于编辑时传递
    private List<String> suitableFor;
    private List<Integer> suitableIds; // 适合人群ID列表，用于编辑时传递
    private List<Map<String, Object>> itinerary;
    private List<String> highlights;
    private List<String> inclusions;
    private List<String> exclusions;
    private List<String> tips;
    private List<Map<String, Object>> faqs;
    private List<Map<String, Object>> images;
} 