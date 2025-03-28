package com.sky.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 员工车辆分配DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeVehicleDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long employeeId; // 员工ID
    private Long vehicleId; // 车辆ID
    private Integer isPrimary; // 是否为主驾驶：0-否，1-是
    private Boolean forceAssign; // 是否强制分配（忽略警告）
    private String notes; // 备注
} 