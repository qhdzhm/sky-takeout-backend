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

    /**
     * 代理商ID
     */
    private Long id;

    /**
     * 登录用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 联系人姓名
     */
    private String contactPerson;

    /**
     * 联系邮箱
     */
    private String email;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 折扣率（0-1之间的小数，例如0.9表示9折）
     */
    private BigDecimal discountRate;

    /**
     * 状态 (0:禁用，1:正常)
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 