package com.sky.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * 一日游数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DayTourDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer id;
    private Integer dayTourId;
    private String name;
    private String description;
    private BigDecimal price;
    // 折扣价格，应用代理商折扣后的价格
    private BigDecimal discountedPrice;
    /** 时长（小时） */
    private String duration;
    private String location;
    private LocalTime departureTime;
    private LocalTime returnTime;
    private Integer minCapacity;
    private Integer maxCapacity;
    private Integer currentBookings;
    private String coverImage;
    private String imageUrl;
    private Integer regionId;
    private String regionName;
    
    // 额外信息
    private BigDecimal rating;
    private String departureAddress;
    private Integer guideId;
    private List<String> themes;
    private List<String> suitableFor;
    
    // 其他字段
    // private BigDecimal childPrice; // 暂时注释，数据库中没有此字段
    private String category;
    // private String pickupInfo; // 暂时注释，数据库中没有此字段
    // private String cancellationPolicy; // 暂时注释，数据库中没有此字段
    private BigDecimal guideFee;
    private Integer[] themeIds;
    private Integer[] suitableIds;

    // 详情信息
    private List<String> highlights; // 亮点
    private List<String> inclusions; // 包含项目
    private List<String> exclusions; // 不包含项目
    private List<Map<String, Object>> faqs; // 常见问题
    private List<String> tips; // 贴士
    private List<Map<String, Object>> images; // 图片
    private List<Map<String, Object>> itinerary; // 行程明细
    
    /**
     * 获取dayTourId，如果为空，则返回id
     */
    public Integer getDayTourId() {
        if (this.dayTourId == null && this.id != null) {
            this.dayTourId = this.id;
        }
        return this.dayTourId;
    }
    
    /**
     * 设置dayTourId，同时设置id保持一致
     */
    public void setDayTourId(Integer dayTourId) {
        this.dayTourId = dayTourId;
        // 始终将dayTourId赋值给id，确保一致性
        if (dayTourId != null) {
            this.id = dayTourId;
        }
    }
    
    /**
     * 获取id，如果为空，则返回dayTourId
     */
    public Integer getId() {
        if (this.id == null && this.dayTourId != null) {
            this.id = this.dayTourId;
        }
        return this.id;
    }
    
    /**
     * 设置id，同时设置dayTourId保持一致
     */
    public void setId(Integer id) {
        this.id = id;
        // 始终将id赋值给dayTourId，确保一致性
        if (id != null) {
            this.dayTourId = id;
        }
    }
} 