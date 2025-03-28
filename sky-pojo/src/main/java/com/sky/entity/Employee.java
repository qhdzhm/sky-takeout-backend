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
    private Integer status; // 状态：0-禁用，1-启用
    private Integer role; // 角色：0-导游，1-操作员，2-管理员
    private Integer workStatus; // 工作状态：0-空闲，1-忙碌，2-休假，3-出团，4-待命
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
    private Long createUser; // 创建人
    private Long updateUser; // 更新人

    // 新增字段：员工驾驶的车辆
    private Vehicle assignedVehicle;
    
    // 新增字段：是否为主驾驶
    private Integer isPrimary;
}
