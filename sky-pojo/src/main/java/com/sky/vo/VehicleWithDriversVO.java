package com.sky.vo;

import com.sky.entity.Employee;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 带有司机信息的车辆VO
 */
@Data
public class VehicleWithDriversVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long vehicleId; // 车辆ID
    private String vehicleType; // 车辆类型
    private String licensePlate; // 车牌号
    private LocalDate regoExpiryDate; // 注册到期日期
    private LocalDate inspectionDueDate; // 检查到期日期
    private Integer status; // 状态：0-送修，1-可用，2-已占用, 100-注册过期, 101-车检过期, 102-驾驶员已满
    private String notes; // 备注
    private Integer maxDrivers; // 最大驾驶员数量
    private String location; // 车辆地址
    private Integer seatCount; // 座位数量
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
    
    private List<Employee> drivers; // 当前驾驶员列表
    private Integer currentDriverCount; // 当前驾驶员数量
    private String allocation; // 分配情况（例如：1/3，表示1个驾驶员，最多3个）
    private Boolean isFull; // 是否已满（当前驾驶员数量 >= 最大驾驶员数量）
    
    // 状态描述，用于前端展示
    private String statusDescription;
    
    // 注册和车检状态标志
    private Boolean isRegoExpired; // 注册是否已过期
    private Boolean isInspectionExpired; // 车检是否已过期
    
    // 剩余天数
    private Long regoRemainingDays; // 注册剩余天数
    private Long inspectionRemainingDays; // 车检剩余天数
} 