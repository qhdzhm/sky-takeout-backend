package com.sky.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employee implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id; // 员工ID
    private String username; // 用户名
    private String name; // 姓名
    private String password; // 密码
    private String phone; // 电话
    private String sex; // 性别
    private String idNumber; // 身份证号
    private Integer role; // 角色：0-导游，1-操作员，2-管理员，3-客服
    private Integer workStatus; // 工作状态：0-空闲，1-忙碌，2-休假，3-出团，4-待命
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
    private Long createUser; // 创建人
    private Long updateUser; // 更新人
    
    // 客服相关字段（仅当role=3时使用）
    private String serviceNo; // 客服工号
    private Integer onlineStatus; // 在线状态：0-离线 1-在线 2-忙碌 3-暂离
    private Integer maxConcurrentCustomers; // 最大同时服务客户数
    private Integer currentCustomerCount; // 当前服务客户数
    private String skillTags; // 客服技能标签
    private Integer serviceLevel; // 客服等级：1-初级 2-中级 3-高级 4-专家
    private LocalDateTime lastLoginTime; // 最后登录时间
    private LocalDateTime lastActiveTime; // 最后活跃时间
    
    // 导游相关字段（仅当role=0时使用）
    private Boolean isGuide; // 是否是导游
    private String guideLevel; // 导游等级
    private String department; // 部门
    private Boolean status; // 状态
}
