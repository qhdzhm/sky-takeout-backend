package com.sky.service;

import com.sky.dto.VehicleDriverDTO;
import com.sky.entity.Employee;
import com.sky.entity.Vehicle;

import java.util.List;

/**
 * 车辆驾驶员服务接口
 */
public interface VehicleDriverService {
    
    /**
     * 分配车辆给员工
     */
    void assignVehicle(VehicleDriverDTO vehicleDriverDTO);
    
    /**
     * 取消分配车辆给员工
     */
    void unassignVehicle(VehicleDriverDTO vehicleDriverDTO);
    
    /**
     * 获取车辆的所有驾驶员
     */
    List<Employee> getDriversByVehicleId(Long vehicleId);
    
    /**
     * 获取员工分配的车辆
     */
    List<Vehicle> getVehiclesByEmployeeId(Long employeeId);
} 