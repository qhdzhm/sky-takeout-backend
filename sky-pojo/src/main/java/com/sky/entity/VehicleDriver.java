package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 车辆驾驶员关联实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDriver implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;             // 主键ID
    private Long vehicleId;      // 车辆ID
    private Long employeeId;     // 员工ID
    private Integer isPrimary;   // 是否为主驾驶：0-副驾驶，1-主驾驶
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
    private String notes;        // 备注
}