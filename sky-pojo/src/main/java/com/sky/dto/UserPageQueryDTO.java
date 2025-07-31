package com.sky.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 用户分页查询DTO
 */
@Data
public class UserPageQueryDTO implements Serializable {

    // 姓名
    private String name;

    // 手机号
    private String phone;
    
    // 用户名
    private String username;

    // 用户类型 regular-普通用户，agent-代理商
    private String userType;

    // 页码
    private int page;

    // 每页记录数
    private int pageSize;
} 