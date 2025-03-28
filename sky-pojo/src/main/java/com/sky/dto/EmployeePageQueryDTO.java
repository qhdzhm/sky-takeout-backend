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
    private Integer role;          // 按角色过滤
    private Integer workStatus;    // 按工作状态过滤
    private Integer status;        // 按账号状态过滤
    private Long assignedVehicleId; // 按分配的车辆ID过滤
    private Boolean hasAssignedVehicle; // 是否已分配车辆
    private LocalDate beginDate;   // 创建时间范围查询
    private LocalDate endDate;
    private String licensePlate;
}
