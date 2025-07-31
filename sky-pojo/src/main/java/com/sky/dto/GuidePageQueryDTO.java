package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 导游分页查询DTO
 */
@Data
public class GuidePageQueryDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // 页码
    private int page;
    
    // 每页记录数
    private int pageSize;
    
    // 导游姓名
    private String name;
    
    // 导游状态
    private Integer status;
}
