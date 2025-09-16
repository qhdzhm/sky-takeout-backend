package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.VehicleStatusConstant;
import com.sky.dto.VehiclePageQueryDTO;
import com.sky.entity.Vehicle;
import com.sky.mapper.VehicleMapper;
import com.sky.result.PageResult;
import com.sky.service.VehicleService;
import com.sky.service.VehicleAvailabilityService;
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

    @Autowired
    private VehicleAvailabilityService vehicleAvailabilityService;

    /**
     * 分页查询车辆
     */
    @Override
    public PageResult pageQuery(VehiclePageQueryDTO vehiclePageQueryDTO) {
        PageHelper.startPage(vehiclePageQueryDTO.getPage(), vehiclePageQueryDTO.getPageSize());
        Page<Vehicle> page = vehicleMapper.pageQuery(vehiclePageQueryDTO);
        
        // 状态计算已在SQL中完成，直接返回结果
        return new PageResult(page.getTotal(), page.getResult());
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
        
        // 新增车辆后，立即同步状态到可用性表
        log.info("新增车辆后同步状态，车辆ID: {}", vehicle.getVehicleId());
        try {
            if (vehicle.getVehicleId() != null) {
                vehicleAvailabilityService.syncSingleVehicleExpiredStatus(vehicle.getVehicleId());
            }
        } catch (Exception e) {
            log.warn("新增车辆后同步状态失败，车辆ID: {}", vehicle.getVehicleId(), e);
        }
    }

    /**
     * 更新车辆信息
     */
    @Override
    public void updateVehicle(Vehicle vehicle) {
        log.info("更新车辆信息，车辆ID: {}", vehicle.getVehicleId());
        
        // 获取更新前的车辆信息，用于比较
        Vehicle oldVehicle = vehicleMapper.getById(vehicle.getVehicleId());
        
        // 更新车辆信息
        vehicleMapper.update(vehicle);
        
        // 检查是否需要同步可用性状态
        boolean needSync = false;
        
        if (oldVehicle != null) {
            // 检查关键状态字段是否发生变化
            boolean regoDateChanged = !isDateEqual(oldVehicle.getRegoExpiryDate(), vehicle.getRegoExpiryDate());
            boolean inspectionDateChanged = !isDateEqual(oldVehicle.getInspectionDueDate(), vehicle.getInspectionDueDate());
            boolean statusChanged = !isIntegerEqual(oldVehicle.getStatus(), vehicle.getStatus());
            
            needSync = regoDateChanged || inspectionDateChanged || statusChanged;
            
            if (needSync) {
                log.info("检测到车辆关键状态变化，需要同步可用性：rego变化={}, 检查变化={}, 状态变化={}", 
                    regoDateChanged, inspectionDateChanged, statusChanged);
            }
        } else {
            // 如果没有找到原车辆信息，也执行同步
            needSync = true;
            log.info("未找到原车辆信息，执行状态同步");
        }
        
        // 如果需要同步，则更新可用性表
        if (needSync) {
            try {
                vehicleAvailabilityService.syncSingleVehicleExpiredStatus(vehicle.getVehicleId());
                log.info("✅ 车辆 {} 状态同步完成", vehicle.getVehicleId());
            } catch (Exception e) {
                log.error("车辆 {} 状态同步失败", vehicle.getVehicleId(), e);
            }
        }
    }

    /**
     * 比较两个日期是否相等（处理null值）
     */
    private boolean isDateEqual(LocalDate date1, LocalDate date2) {
        if (date1 == null && date2 == null) {
            return true;
        }
        if (date1 == null || date2 == null) {
            return false;
        }
        return date1.equals(date2);
    }

    /**
     * 比较两个整数是否相等（处理null值）
     */
    private boolean isIntegerEqual(Integer int1, Integer int2) {
        if (int1 == null && int2 == null) {
            return true;
        }
        if (int1 == null || int2 == null) {
            return false;
        }
        return int1.equals(int2);
    }

    /**
     * 删除车辆
     */
    @Override
    public void deleteVehicle(Long id) {
        vehicleMapper.deleteById(id);
    }
}