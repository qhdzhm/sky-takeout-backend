package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 旅游代理商实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Agent implements Serializable {

    private static final long serialVersionUID = 1L;

    // 代理商ID
    private Long id;

    // 登录用户名
    private String username;

    // 登录密码
    private String password;

    // 公司名称
    private String companyName;
    
    // 联系人姓名
    private String contactPerson;

    // 联系电话
    private String phone;

    // 电子邮箱
    private String email;
    
    // 公司地址
    private String address;
    
    // 折扣比例，1.00表示无折扣，0.90表示9折
    private BigDecimal discountRate;
    
    // 状态：1-活跃，0-禁用
    private Integer status;
    
    // 创建时间
    private LocalDateTime createdAt;
    
    // 更新时间
    private LocalDateTime updatedAt;
} 