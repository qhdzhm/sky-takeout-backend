package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 一日游与适合人群关联实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DayTourSuitable implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关联ID
     */
    private Integer id;

    /**
     * 一日游ID
     */
    private Integer dayTourId;

    /**
     * 适合人群ID
     */
    private Integer suitableId;
} 