package com.sky.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 地区数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegionDTO {
    private Integer id;
    private String name;
    private String description;
    private String imageUrl;
    private Integer tourCount; // 该地区的旅游产品数量
} 