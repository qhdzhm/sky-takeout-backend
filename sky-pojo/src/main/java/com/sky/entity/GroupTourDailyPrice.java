package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 团队游每日价格实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupTourDailyPrice implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 团队游ID */
    private Integer groupTourId;

    /** 价格日期 */
    private LocalDate priceDate;

    /** 当日价格（元/人） */
    private BigDecimal dailyPrice;

    /** 备注信息 */
    private String notes;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}







