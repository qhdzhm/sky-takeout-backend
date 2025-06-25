package com.sky.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 行程选项DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItineraryOptionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关联表ID
     */
    private Integer id;

    /**
     * 一日游ID
     */
    private Integer dayTourId;

    /**
     * 一日游名称
     */
    private String dayTourName;

    /**
     * 一日游描述
     */
    private String dayTourDescription;

    /**
     * 一日游地点
     */
    private String dayTourLocation;

    /**
     * 一日游时长
     */
    private String dayTourDuration;

    /**
     * 一日游基础价格
     */
    private BigDecimal dayTourPrice;

    /**
     * 价格差异（相对于基准价）
     */
    private BigDecimal priceDifference;

    /**
     * 是否为默认选项
     */
    private Boolean isDefault;

    /**
     * 一日游图片
     */
    private String dayTourImage;

    /**
     * 选项组名称
     */
    private String optionGroupName;
} 