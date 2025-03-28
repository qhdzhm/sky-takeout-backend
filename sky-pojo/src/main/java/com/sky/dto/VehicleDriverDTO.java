package com.sky.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 车辆驾驶员DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDriverDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id; // 关联ID
    private Long vehicleId; // 车辆ID
    private Long employeeId; // 员工ID
    private Integer isPrimary; // 是否为主驾驶：0-否，1-是
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
    private String notes; // 备注
    private Boolean forceAssign; // 是否强制分配（忽略警告）
} 