package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 预订行程选择实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingItineraryChoice implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 预订ID
     */
    private Integer bookingId;

    /**
     * 天数（第几天）
     */
    private Integer dayNumber;

    /**
     * 选择的一日游ID
     */
    private Integer selectedDayTourId;

    /**
     * 选项组名称（如：第2天选择）
     */
    private String optionGroupName;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 一日游名称（非数据库字段，用于显示）
     */
    private String dayTourName;

    /**
     * 价格差异（非数据库字段，用于显示）
     */
    private java.math.BigDecimal priceDifference;
} 