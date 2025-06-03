package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 客服分页查询DTO
 */
@Data
public class CustomerServicePageQueryDTO implements Serializable {

    // 页码
    private int page;

    // 页大小
    private int pageSize;

    // 姓名
    private String name;

    // 在线状态
    private Integer onlineStatus;

    // 服务等级
    private Integer serviceLevel;
} 