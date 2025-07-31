package com.sky.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 旅游产品请求参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TourRequestParams implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 页码
    private Integer page;
    
    // 每页记录数
    private Integer pageSize;
    
    // 搜索关键词
    private String keyword;
    
    // 旅游类型：day_tour(一日游)或group_tour(跟团游)
    private String tourType;
    
    // 地区ID
    private Integer regionId;
    
    // 地点名称
    private String location;
    
    // 主题列表
    private List<String> themes;
    
    // 评分范围
    private BigDecimal minRating;
    
    // 价格范围
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    
    // 时长范围(天)
    private Integer minDuration;
    private Integer maxDuration;
    
    // 适合人群
    private List<String> suitableFor;
    
    // 出发日期范围
    private LocalDate startDate;
    private LocalDate endDate;
    
    // 代理商ID，用于计算折扣价格
    private Long agentId;
} 