package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 一日游主题实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DayTourTheme implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主题ID
     */
    private Integer themeId;

    /**
     * 名称
     */
    private String name;
} 