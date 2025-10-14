package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 职位视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PositionVO implements Serializable {

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

    // 部门名称
    private String deptName;

    // 职位层级
    private Integer positionLevel;

    // 是否管理岗位
    private Integer isManagement;

    // 对应原role字段值
    private Integer legacyRoleMapping;

    // 是否可分配订单
    private Integer canAssignOrders;

    // 操作员类型
    private String operatorType;

    // 职位描述
    private String description;

    // 状态
    private Integer status;

    // 创建时间
    private LocalDateTime createdAt;

    // 更新时间
    private LocalDateTime updatedAt;

    // 员工数量
    private Integer employeeCount;

    // 权限数量
    private Integer permissionCount;

    // 最后权限更新时间
    private LocalDateTime lastPermissionUpdate;
}


