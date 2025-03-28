package com.sky.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * ClassName: VehiclePageQueryDTO
 * Package: com.sky.dto
 * Description:
 *
 * @Author Tangshifu
 * @Create 2025/3/11 18:19
 * @Version 1.0
 */
@Data
public class VehiclePageQueryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int page = 1;          // 默认第1页
    private int pageSize = 10;     // 默认每页10条

    // 查询条件
    private String licensePlate;   // 车牌号模糊查询
    private String vehicleType;    // 车辆类型
    private Integer status;        // 状态过滤
    private LocalDate regoExpiryDate; // 注册到期日期范围
    private LocalDate inspectionDueDate; // 检查到期日期范围
    private Integer seatCountMin;  // 座位数最小值
    private Integer seatCountMax;  // 座位数最大值
    private String location;       // 车辆地址模糊查询
    private Boolean hasAvailableDrivers; // 是否有可用驾驶员
    private String driverName;
    private Integer seatCount;
}