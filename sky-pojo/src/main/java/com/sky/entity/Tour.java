package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 通用旅游实体类，用于合并一日游和跟团游数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tour implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Integer id;
    
    /** 名称 */
    private String name;
    
    /** 描述 */
    private String description;
    
    /** 价格 */
    private BigDecimal price;
    
    /** 时长 */
    private String duration;
    
    /** 出发地点 */
    private String location;
    
    /** 最小人数 */
    private Integer minPeople;
    
    /** 最大人数 */
    private Integer maxPeople;
    
    /** 当前预订人数 */
    private Integer currentBookings;
    
    /** 封面图片 */
    private String coverImage;
    
    /** 旅游类型：day-一日游，group-跟团游 */
    private String tourType;
    
    // 一日游特有字段
    /** 出发时间 */
    private LocalTime departureTime;
    
    /** 返回时间 */
    private LocalTime returnTime;
    
    // 跟团游特有字段
    /** 开始日期 */
    private LocalDate startDate;
    
    /** 结束日期 */
    private LocalDate endDate;
    
    /** 地区ID */
    private Integer regionId;
    
    /** 地区名称（非数据库字段，关联查询） */
    private String regionName;
    
    /** 包含项目（非数据库字段，从tour_inclusions表关联查询） */
    private String inclusions;
} 