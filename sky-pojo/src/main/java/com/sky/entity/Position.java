package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 职位实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Position implements Serializable {

    private static final long serialVersionUID = 1L;

    // 主键
    private Long id;

    // 职位代码
    private String positionCode;

    // 职位名称
    private String positionName;

    // 职位英文名称
    private String positionNameEn;

    // 所属部门ID
    private Long deptId;

    // 职位层级 1-高管 2-经理 3-主管 4-专员 5-普通员工
    private Integer positionLevel;

    // 是否管理岗位 1-是 0-否
    private Integer isManagement;

    // 对应原role字段值(兼容用)
    private Integer legacyRoleMapping;

    // 是否可分配订单
    private Integer canAssignOrders;

    // 操作员类型(兼容用)
    private String operatorType;

    // 页面访问权限JSON
    private String pagePermissions;

    // 职位描述
    private String description;

    // 状态 1-启用 0-禁用
    private Integer status;

    // 创建时间
    private LocalDateTime createdAt;

    // 更新时间
    private LocalDateTime updatedAt;
}

