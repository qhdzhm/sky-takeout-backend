package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 跟团游实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupTour implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Integer id;
    
    /** 名称 */
    private String name;
    
    /** 描述 */
    private String description;
    
    /** 价格 */
    private BigDecimal price;
    
    /** 时长（天） */
    private String duration;
    
    /** 出发地点 */
    private String location;
    
    /** 开始日期 */
    private LocalDate startDate;
    
    /** 结束日期 */
    private LocalDate endDate;
    
    /** 最小人数 */
    private Integer minPeople;
    
    /** 最大人数 */
    private Integer maxPeople;
    
    /** 当前预订人数 */
    private Integer currentBookings;
    
    /** 封面图片 */
    private String coverImage;
    
    /** 地区ID */
    private Integer regionId;
    
    /** 地区名称（非数据库字段，关联查询） */
    private String regionName;
} 