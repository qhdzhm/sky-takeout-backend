package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 一日游实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DayTour implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 一日游ID
     */
    private Integer dayTourId;

    /**
     * 名称
     */
    private String name;

    /**
     * 位置
     */
    private String location;

    /**
     * 描述
     */
    private String description;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 儿童价格
     */
    private BigDecimal childPrice;

    /**
     * 时长
     */
    private String duration;

    /**
     * 评分
     */
    private BigDecimal rating;

    /**
     * 主图URL
     */
    private String imageUrl;

    /**
     * 类别
     */
    private String category;

    /**
     * 是否激活
     */
    private Integer isActive;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 区域ID
     */
    private Integer regionId;

    /**
     * 出发地址
     */
    private String departureAddress;

    /**
     * 导游费用
     */
    private BigDecimal guideFee;

    /**
     * 导游ID
     */
    private Integer guideId;

    /**
     * 接送信息
     */
    private String pickupInfo;

    /**
     * 取消政策
     */
    private String cancellationPolicy;
} 