package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * 一日游实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DayTour implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Integer id;
    
    /** 一日游ID (与id保持一致) */
    private Integer dayTourId;
    
    /** 名称 */
    private String name;
    
    /** 描述 */
    private String description;
    
    /** 简短描述（用于标题下方） */
    private String shortDescription;
    
    /** 概述配图URL */
    private String overviewImage;
    
    /** 价格 */
    private BigDecimal price;
    
    /** 时长（小时） */
    private String duration;
    
    /** 出发地点 */
    private String location;
    
    /** 出发时间 */
    private LocalTime departureTime;
    
    /** 返回时间 */
    private LocalTime returnTime;
    
    /** 最小人数 */
    private Integer minCapacity;
    
    /** 最大人数 */
    private Integer maxCapacity;
    
    /** 当前预订人数 */
    private Integer currentBookings;
    
    /** 封面图片 */
    private String coverImage;
    
    /** Banner背景图片URL */
    private String bannerImage;
    
    /** 产品展示图片URL */
    private String productShowcaseImage;
    
    /** 主图URL */
    private String imageUrl;
    
    /** 分类/类别 */
    private String category;
    
    /** 评分 */
    private BigDecimal rating;
    
    /** 是否激活 */
    private Integer isActive;
    
    /** 地区ID */
    private Integer regionId;
    
    /** 出发地址 */
    private String departureAddress;
    
    /** 导游费用 */
    private BigDecimal guideFee;
    
    /** 导游ID */
    private Integer guideId;
    
    /** 成本价格 */
    private BigDecimal costPrice;
    
    /** 地区名称（非数据库字段，关联查询） */
    private String regionName;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
    
    /** 更新时间 */
    private LocalDateTime updatedAt;
    
    /**
     * 获取dayTourId，如果为空，则返回id
     * 确保永远不会返回null（除非两个ID都是null）
     */
    public Integer getDayTourId() {
        if (this.dayTourId == null && this.id != null) {
            this.dayTourId = this.id;
        }
        return this.dayTourId;
    }
    
    /**
     * 设置dayTourId，同时设置id保持一致
     * 确保两个ID字段始终保持同步
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
     * 确保永远不会返回null（除非两个ID都是null）
     */
    public Integer getId() {
        if (this.id == null && this.dayTourId != null) {
            this.id = this.dayTourId;
        }
        return this.id;
    }
    
    /**
     * 设置id，同时设置dayTourId保持一致
     * 确保两个ID字段始终保持同步
     */
    public void setId(Integer id) {
        this.id = id;
        // 始终将id赋值给dayTourId，确保一致性
        if (id != null) {
            this.dayTourId = id;
        }
    }
} 