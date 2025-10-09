package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统页面实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemPage implements Serializable {

    private static final long serialVersionUID = 1L;

    // 主键
    private Long id;

    // 页面路径
    private String pagePath;

    // 页面显示名称
    private String pageName;

    // 页面分组
    private String pageGroup;

    // 分组图标
    private String pageGroupIcon;

    // 页面图标
    private String pageIcon;

    // 权限等级 basic, management, advanced, finance, system
    private String permissionLevel;

    // 是否必需权限(如仪表盘)
    private Integer isRequired;

    // 页面功能描述
    private String description;

    // 排序
    private Integer sortOrder;

    // 状态 1-启用 0-禁用
    private Integer status;

    // 创建时间
    private LocalDateTime createdAt;

    // 更新时间
    private LocalDateTime updatedAt;
}

