package com.sky.service;

import com.sky.dto.VehiclePageQueryDTO;
import com.sky.entity.Vehicle;
import com.sky.result.PageResult;
import com.sky.vo.VehicleWithDriversVO;

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
    
    /**
     * 获取车辆及其驾驶员信息
     */
    VehicleWithDriversVO getVehicleWithDrivers(Long id);
    
    /**
     * 获取所有可分配车辆列表（包含驾驶员数量信息）
     */
    List<VehicleWithDriversVO> getAllAvailableVehicles();
}