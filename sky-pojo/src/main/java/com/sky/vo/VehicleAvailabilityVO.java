package com.sky.vo;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 车辆可用性VO
 */
@Data
public class VehicleAvailabilityVO {
    
    private Long vehicleId;
    
    /**
     * 可用日期
     */
    private LocalDate date;
    
    /**
     * 车辆类型
     */
    private String vehicleType;
    
    /**
     * 车牌号
     */
    private String licensePlate;
    
    /**
     * 座位数
     */
    private Integer seatCount;
    
    /**
     * 当前位置
     */
    private String currentLocation;
    
    /**
     * 可用开始时间
     */
    private LocalTime availableStartTime;
    
    /**
     * 可用结束时间
     */
    private LocalTime availableEndTime;
    
    /**
     * 油量百分比
     */
    private Double fuelLevel;
    
    /**
     * 里程数
     */
    private Integer mileage;
    
    /**
     * 可用状态
     */
    private String status;
    
    /**
     * 备注
     */
    private String notes;
    
    /**
     * 最大分配数量
     */
    private Integer maxGroups;
    
    /**
     * 当前分配数量
     */
    private Integer currentGroups;
    
    /**
     * 是否推荐
     */
    private Boolean recommended;
    
    /**
     * 推荐原因
     */
    private String recommendReason;
    
    /**
     * 司机信息
     */
    private String driverInfo;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 