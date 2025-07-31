package com.sky.service;

import com.sky.dto.VehiclePageQueryDTO;
import com.sky.entity.Vehicle;
import com.sky.result.PageResult;

import java.util.List;

public interface VehicleService {

    /**
     * 分页查询车辆
     */
    PageResult pageQuery(VehiclePageQueryDTO vehiclePageQueryDTO);

    /**
     * 根据ID查询车辆
     */
    Vehicle getById(Long id);

    /**
     * 新增车辆
     */
    void addVehicle(Vehicle vehicle);

    /**
     * 更新车辆信息
     */
    void updateVehicle(Vehicle vehicle);

    /**
     * 删除车辆
     */
    void deleteVehicle(Long id);
}