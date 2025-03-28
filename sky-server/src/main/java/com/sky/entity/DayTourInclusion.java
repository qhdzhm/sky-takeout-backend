package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 一日游包含项实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DayTourInclusion implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Integer id;

    /**
     * 一日游ID
     */
    private Integer dayTourId;

    /**
     * 描述
     */
    private String description;

    /**
     * 排序位置
     */
    private Integer position;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
} 