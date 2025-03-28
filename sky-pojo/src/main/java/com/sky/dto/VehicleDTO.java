package com.sky.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ClassName: VehicleDTO
 * Package: com.sky.dto
 * Description:
 *
 * @Author Tangshifu
 * @Create 2025/3/11 18:18
 * @Version 1.0
 */
public class VehicleDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long vehicleId;

    @NotBlank(message = "车辆类型不能为空")
    private String vehicleType;

    @NotBlank(message = "车牌号不能为空")
    private String licensePlate;

    @NotNull(message = "注册到期日期不能为空")
    private LocalDate regoExpiryDate;

    @NotNull(message = "检查到期日期不能为空")
    private LocalDate inspectionDueDate;

    @NotNull(message = "状态不能为空")
    private Integer status;

    private String notes;

    @NotNull(message = "最大驾驶员数量不能为空")
    @Min(value = 1, message = "最大驾驶员数量至少为1")
    private Integer maxDrivers;

    private String location;

    @NotNull(message = "座位数量不能为空")
    @Min(value = 1, message = "座位数量至少为1")
    private Integer seatCount;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 分配的驾驶员列表
    private List<EmployeeDTO> assignedDrivers;

    // 以下字段不对应数据库表字段，用于传输数据
    private Integer currentDriverCount; // 当前驾驶员数量
    private List<String> driverNames; // 驾驶员名称列表

    // getters/setters
}