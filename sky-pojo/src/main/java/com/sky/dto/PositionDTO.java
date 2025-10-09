package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 职位数据传输对象
 */
@Data
public class PositionDTO implements Serializable {

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

    // 职位层级
    private Integer positionLevel;

    // 是否管理岗位
    private Integer isManagement;

    // 对应原role字段值(兼容用)
    private Integer legacyRoleMapping;

    // 是否可分配订单
    private Integer canAssignOrders;

    // 操作员类型(兼容用)
    private String operatorType;

    // 职位描述
    private String description;

    // 状态
    private Integer status;
}

