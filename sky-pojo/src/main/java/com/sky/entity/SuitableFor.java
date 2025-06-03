package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 适合人群实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuitableFor implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 适合人群ID
     */
    private Integer suitableId;

    /**
     * 适合人群名称
     */
    private String name;

    /**
     * 适合人群描述
     */
    private String description;
} 