package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 代理商实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Agent implements Serializable {

    private static final long serialVersionUID = 1L;

    // 代理商ID
    private Long id;
    
    // 用户名
    private String username;
    
    // 密码
    private String password;
    
    // 公司名称
    private String companyName;
    
    // 联系人姓名
    private String contactPerson;
    
    // 联系电话
    private String phone;
    
    // 电子邮箱
    private String email;
    
    // 折扣率（保留兼容性，新系统使用折扣等级）
    private BigDecimal discountRate;
    
    // 折扣等级ID
    private Long discountLevelId;
    
    // 账号状态：1-活跃，0-禁用
    private Integer status;
    
    // 创建时间
    private LocalDateTime createdAt;
    
    // 更新时间
    private LocalDateTime updatedAt;
} 