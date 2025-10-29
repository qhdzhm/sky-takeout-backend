package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 一日游分页查询DTO
 */
@Data
public class DayTourPageQueryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 页码
     */
    private int page;

    /**
     * 每页记录数
     */
    private int pageSize;

    /**
     * 名称
     */
    private String name;

    /**
     * 类别
     */
    private String category;

    /**
     * 位置
     */
    private String location;

    /**
     * 价格下限
     */
    private Double minPrice;

    /**
     * 价格上限
     */
    private Double maxPrice;

    /**
     * 评分下限
     */
    private Double minRating;

    /**
     * 区域ID
     */
    private Integer regionId;

    /**
     * 是否激活 0-禁用 1-启用
     */
    private Integer isActive;
    
    /**
     * 是否在用户端显示 0-隐藏 1-显示
     */
    private Integer showOnUserSite;
    
    /**
     * 排序字段
     */
    private String orderBy;
} 