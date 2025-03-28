package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 图片视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageVO implements Serializable {
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

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
} 