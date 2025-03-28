package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 跟团游图片实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupTourImage implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Integer id;
    
    /** 跟团游ID */
    private Integer groupTourId;
    
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