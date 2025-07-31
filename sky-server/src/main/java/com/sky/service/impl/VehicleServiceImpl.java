package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.VehicleStatusConstant;
import com.sky.dto.VehiclePageQueryDTO;
import com.sky.entity.Vehicle;
import com.sky.mapper.VehicleMapper;
import com.sky.result.PageResult;
import com.sky.service.VehicleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class VehicleServiceImpl implements VehicleService {

    @Autowired
    private VehicleMapper vehicleMapper;

    /**
     * 分页查询车辆
     */
    @Override
    public PageResult pageQuery(VehiclePageQueryDTO vehiclePageQueryDTO) {
        PageHelper.startPage(vehiclePageQueryDTO.getPage(), vehiclePageQueryDTO.getPageSize());
        Page<Vehicle> page = vehicleMapper.pageQuery(vehiclePageQueryDTO);
        
        // 获取车辆列表
        List<Vehicle> vehicles = page.getResult();
        
        // 当前日期，用于比较到期日期
        LocalDate today = LocalDate.now();
        
        // 为每个车辆更新状态
        List<Vehicle> filteredVehicles = new ArrayList<>();
        for (Vehicle vehicle : vehicles) {
            // 更新车辆状态
            updateVehicleStatus(vehicle, today);
            
            // 如果指定了状态过滤，只添加匹配的车辆
            if (vehiclePageQueryDTO.getStatus() == null || vehicle.getStatus().equals(vehiclePageQueryDTO.getStatus())) {
                filteredVehicles.add(vehicle);
            }
        }
        
        // 更新总数和过滤后的列表
        if (vehiclePageQueryDTO.getStatus() != null) {
            page.setTotal(filteredVehicles.size());
        }
        
        return new PageResult(page.getTotal(), filteredVehicles);
    }

    /**
     * 更新车辆状态
     * 状态优先级：
     * 1. 送修状态（手动设置为0）
     * 2. 注册过期 (4)
     * 3. 车检过期 (5)
     * 4. 可用 (1)
     */
    private void updateVehicleStatus(Vehicle vehicle, LocalDate today) {
        // 如果已经是送修状态，保持不变
        if (vehicle.getStatus() != null && vehicle.getStatus() == VehicleStatusConstant.DISABLED) {
            return;
        }
        
        // 检查注册到期情况
        if (vehicle.getRegoExpiryDate() != null && today.isAfter(vehicle.getRegoExpiryDate())) {
            vehicle.setStatus(VehicleStatusConstant.REGO_EXPIRED);
            return;
        }
        
        // 检查车检到期情况
        if (vehicle.getInspectionDueDate() != null && today.isAfter(vehicle.getInspectionDueDate())) {
            vehicle.setStatus(VehicleStatusConstant.INSPECTION_EXPIRED);
            return;
        }
        
        // 默认设置为可用状态
        vehicle.setStatus(VehicleStatusConstant.AVAILABLE);
    }

    /**
     * 根据ID查询车辆
     */
    @Override
    public Vehicle getById(Long id) {
        Vehicle vehicle = vehicleMapper.getById(id);
        if (vehicle != null && vehicle.getStatus() == null) {
            // 如果状态为null，设置默认状态为可用
            vehicle.setStatus(VehicleStatusConstant.AVAILABLE);
        }
        return vehicle;
    }

    /**
     * 新增车辆
     */
    @Override
    public void addVehicle(Vehicle vehicle) {
        vehicleMapper.insert(vehicle);
    }

    /**
     * 更新车辆信息
     */
    @Override
    public void updateVehicle(Vehicle vehicle) {
        vehicleMapper.update(vehicle);
    }

    /**
     * 删除车辆
     */
    @Override
    public void deleteVehicle(Long id) {
        vehicleMapper.deleteById(id);
    }
}