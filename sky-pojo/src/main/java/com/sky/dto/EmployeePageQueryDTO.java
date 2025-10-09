package com.sky.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class EmployeePageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private int page = 1;          // 默认第1页
    private int pageSize = 10;     // 默认每页10条

    // 查询条件
    private String name;           // 姓名模糊查询
    private String role;           // 按职位名称过滤（模糊匹配）
    private Integer workStatus;    // 按工作状态过滤
    private Integer status;        // 按账号状态过滤
    private Long assignedVehicleId; // 按分配的车辆ID过滤
    private Boolean hasAssignedVehicle; // 是否已分配车辆
    private LocalDate beginDate;   // 创建时间范围查询
    private LocalDate endDate;
    private String licensePlate;
    private Long deptId;        // 按部门ID过滤（用于部门权限控制）
}
