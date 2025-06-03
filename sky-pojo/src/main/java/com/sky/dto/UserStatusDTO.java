package com.sky.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 用户状态修改DTO
 */
@Data
public class UserStatusDTO implements Serializable {

    // 用户ID
    private Long id;
    
    // 状态 0-禁用，1-启用
    private Integer status;
} 