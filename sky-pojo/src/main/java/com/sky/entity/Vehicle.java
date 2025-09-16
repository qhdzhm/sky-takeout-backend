package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ClassName: Vehicle
 * Package: com.sky.entity
 * Description:
 *
 * @Author Tangshifu
 * @Create 2025/3/11 17:48
 * @Version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long vehicleId; // 车辆ID
    private String vehicleType; // 车辆类型
    private String licensePlate; // 车牌号
    private LocalDate regoExpiryDate; // 注册到期日期
    private LocalDate inspectionDueDate; // 检查到期日期
    private Integer status; // 状态：0-送修中，1-可用，4-注册过期，5-车检过期
    private String notes; // 备注
    private String location; // 车辆地址
    private Integer seatCount; // 座位数量
    private Integer maxDrivers; // 最大驾驶员数量
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
}
