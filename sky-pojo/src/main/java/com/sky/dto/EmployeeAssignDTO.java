package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 员工分配数据传输对象
 */
@Data
public class EmployeeAssignDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // 员工ID
    private Long employeeId;

    // 部门ID
    private Long deptId;

    // 职位ID
    private Long positionId;

    // 直属上级ID
    private Long directSupervisorId;

    // 操作说明
    private String description;
}


