package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 员工详细信息VO（包含部门职位信息）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeWithDeptVO implements Serializable {

    private static final long serialVersionUID = 1L;

    // 员工基本信息
    private Long id;
    private String name;
    private String username;
    private String role;
    private Integer status;
    
    // 部门信息
    private Long deptId;
    private String deptName;
    
    // 职位信息
    private Long positionId;
    private String positionName;
    private Integer positionLevel;
    
    // 职位额外信息
    private Integer isManagement;
    private Integer legacyRoleMapping;
    
    // 时间信息
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
