package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 客服实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerService implements Serializable {

    private static final long serialVersionUID = 1L;

    // 客服ID
    private Long id;

    // 客服工号
    private String serviceNo;

    // 客服姓名
    private String name;

    // 客服用户名
    private String username;

    // 密码
    private String password;

    // 手机号
    private String phone;

    // 邮箱
    private String email;

    // 头像
    private String avatar;

    // 在线状态 0-离线 1-在线 2-忙碌 3-暂离
    private Integer onlineStatus;

    // 最大同时服务客户数
    private Integer maxConcurrentCustomers;

    // 当前服务客户数
    private Integer currentCustomerCount;

    // 客服技能标签 (如：旅游咨询,退票改签,投诉处理等)
    private String skillTags;

    // 客服等级 1-初级 2-中级 3-高级 4-专家
    private Integer serviceLevel;

    // 状态 0-禁用 1-正常
    private Integer status;

    // 创建时间
    private LocalDateTime createTime;

    // 更新时间
    private LocalDateTime updateTime;

    // 最后登录时间
    private LocalDateTime lastLoginTime;

    // 最后活跃时间
    private LocalDateTime lastActiveTime;
} 