package com.sky.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 图片数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 图片ID */
    private Integer id;

    /** 关联ID (一日游ID或团队游ID) */
    private Integer relatedId;

    /** 图片类型 (day_tour, group_tour) */
    private String type;

    /** 图片URL */
    private String imageUrl;

    /** 缩略图URL */
    private String thumbnailUrl;

    /** 图片描述 */
    private String description;

    /** 是否为主图 (0=否, 1=是) */
    private Integer isPrimary;

    /** 排序号 */
    private Integer position;
} 