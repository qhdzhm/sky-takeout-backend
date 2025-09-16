package com.sky.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * 员工角色更新DTO
 */
@Data
public class EmployeeRoleUpdateDTO {
    
    @NotNull(message = "员工ID不能为空")
    private Long employeeId;
    
    @NotBlank(message = "操作员类型不能为空")
    @Pattern(regexp = "^(tour_master|hotel_operator|general)$", 
             message = "操作员类型只能是 tour_master、hotel_operator 或 general")
    private String operatorType;
    
    private String reason; // 变更原因
    
    private Boolean canAssignOrders; // 是否有分配权限
}

