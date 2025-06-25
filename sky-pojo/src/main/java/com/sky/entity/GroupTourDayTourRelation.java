package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 跟团游与一日游关联实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupTourDayTourRelation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 团队游ID
     */
    private Integer groupTourId;

    /**
     * 一日游ID
     */
    private Integer dayTourId;

    /**
     * 天数（第几天）
     */
    private Integer dayNumber;

    /**
     * 是否为可选项（0-必选，1-可选）
     */
    private Integer isOptional;

    /**
     * 选项组名称（如：第2天选择）
     */
    private String optionGroupName;

    /**
     * 价格差异（相对于基准价）
     */
    private BigDecimal priceDifference;

    /**
     * 是否为默认选项
     */
    private Integer isDefault;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    // 以下为关联查询字段，非数据库字段
    /**
     * 一日游名称
     */
    private String dayTourName;

    /**
     * 一日游描述
     */
    private String dayTourDescription;

    /**
     * 一日游价格
     */
    private BigDecimal dayTourPrice;

    /**
     * 一日游地点
     */
    private String dayTourLocation;

    /**
     * 一日游时长
     */
    private String dayTourDuration;

    /**
     * 一日游图片
     */
    private String dayTourImage;
} 