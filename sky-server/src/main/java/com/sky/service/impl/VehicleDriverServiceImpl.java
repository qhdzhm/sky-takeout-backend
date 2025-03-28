package com.sky.service.impl;

import com.sky.constant.VehicleStatusConstant;
import com.sky.dto.VehicleDriverDTO;
import com.sky.entity.Employee;
import com.sky.entity.Vehicle;
import com.sky.entity.VehicleDriver;
import com.sky.exception.BusinessException;
import com.sky.mapper.EmployeeMapper;
import com.sky.mapper.VehicleDriverMapper;
import com.sky.mapper.VehicleMapper;
import com.sky.service.VehicleDriverService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class VehicleDriverServiceImpl implements VehicleDriverService {

    @Autowired
    private VehicleDriverMapper vehicleDriverMapper;
    
    @Autowired
    private VehicleMapper vehicleMapper;
    
    @Autowired
    private EmployeeMapper employeeMapper;
    
    /**
     * 分配车辆给员工
     */
    @Override
    @Transactional
    public void assignVehicle(VehicleDriverDTO vehicleDriverDTO) {
        Long vehicleId = vehicleDriverDTO.getVehicleId();
        Long employeeId = vehicleDriverDTO.getEmployeeId();
        Integer isPrimary = vehicleDriverDTO.getIsPrimary();
        
        // 检查车辆是否存在
        Vehicle vehicle = vehicleMapper.getById(vehicleId);
        if (vehicle == null) {
            throw new BusinessException("车辆不存在");
        }
        
        // 检查员工是否存在
        Employee employee = employeeMapper.getById(employeeId.intValue());
        if (employee == null) {
            throw new BusinessException("员工不存在");
        }
        
        // 检查车辆状态是否可分配
        if (vehicle.getStatus() == null || vehicle.getStatus() != VehicleStatusConstant.AVAILABLE) {
            if (vehicle.getStatus() == VehicleStatusConstant.DISABLED) {
                throw new BusinessException("车辆正在送修中，无法分配");
            } else if (vehicle.getStatus() == VehicleStatusConstant.REGO_EXPIRED) {
                throw new BusinessException("车辆注册已过期，无法分配");
            } else if (vehicle.getStatus() == VehicleStatusConstant.INSPECTION_EXPIRED) {
                throw new BusinessException("车辆车检已过期，无法分配");
            } else if (vehicle.getStatus() == VehicleStatusConstant.FULL) {
                throw new BusinessException("车辆驾驶员已满，无法分配");
            } else {
                throw new BusinessException("车辆状态异常，无法分配");
            }
        }
        
        // 检查当前日期与注册/车检到期日期
        LocalDate today = LocalDate.now();
        if (vehicle.getRegoExpiryDate() != null && today.isAfter(vehicle.getRegoExpiryDate())) {
            throw new BusinessException("车辆注册已过期，无法分配");
        }
        
        if (vehicle.getInspectionDueDate() != null && today.isAfter(vehicle.getInspectionDueDate())) {
            throw new BusinessException("车辆车检已过期，无法分配");
        }
        
        // 检查车辆是否已满
        Integer currentDriverCount = vehicleDriverMapper.countDriversByVehicleId(vehicleId);
        if (currentDriverCount >= vehicle.getMaxDrivers()) {
            throw new BusinessException("车辆驾驶员数量已满，无法分配");
        }
        
        // 检查员工状态是否可分配
        if (employee.getStatus() == null || employee.getStatus() != 1) {
            throw new BusinessException("员工状态异常，无法分配车辆");
        }
        
        // 检查员工工作状态
        if (employee.getWorkStatus() != null) {
            if (employee.getWorkStatus() == 1) {
                throw new BusinessException("员工当前处于忙碌状态，请确认是否继续分配车辆", true);
            } else if (employee.getWorkStatus() == 2) {
                throw new BusinessException("员工当前处于休假状态，请确认是否继续分配车辆", true);
            } else if (employee.getWorkStatus() == 3) {
                throw new BusinessException("员工当前处于出团状态，请确认是否继续分配车辆", true);
            }
        }
        
        // 检查员工是否已分配车辆
        List<VehicleDriver> assignedVehicles = vehicleDriverMapper.getByEmployeeId(employeeId);
        if (assignedVehicles != null && !assignedVehicles.isEmpty()) {
            throw new BusinessException("员工已分配车辆，请确认是否继续分配", true);
        }
        
        // 创建车辆-驾驶员关联
        VehicleDriver vehicleDriver = VehicleDriver.builder()
                .vehicleId(vehicleId)
                .employeeId(employeeId)
                .isPrimary(isPrimary)
                .build();
        
        vehicleDriverMapper.insert(vehicleDriver);
        
        // 更新车辆状态
        Integer newDriverCount = currentDriverCount + 1;
        if (newDriverCount == vehicle.getMaxDrivers()) {
            vehicle.setStatus(VehicleStatusConstant.FULL); // 驾驶员已满
        } else {
            vehicle.setStatus(VehicleStatusConstant.OCCUPIED); // 已占用
        }
        vehicleMapper.update(vehicle);
        
        // 更新员工工作状态为忙碌
        employee.setWorkStatus(1); // 忙碌状态
        employeeMapper.update(employee);
    }
    
    /**
     * 取消分配车辆给员工
     */
    @Override
    @Transactional
    public void unassignVehicle(VehicleDriverDTO vehicleDriverDTO) {
        Long vehicleId = vehicleDriverDTO.getVehicleId();
        Long employeeId = vehicleDriverDTO.getEmployeeId();
        
        // 检查车辆是否存在
        Vehicle vehicle = vehicleMapper.getById(vehicleId);
        if (vehicle == null) {
            throw new BusinessException("车辆不存在");
        }
        
        // 检查员工是否存在
        Employee employee = employeeMapper.getById(employeeId.intValue());
        if (employee == null) {
            throw new BusinessException("员工不存在");
        }
        
        // 检查员工是否分配了该车辆
        VehicleDriver vehicleDriver = vehicleDriverMapper.getByVehicleIdAndEmployeeId(vehicleId, employeeId);
        if (vehicleDriver == null) {
            throw new BusinessException("该员工未分配此车辆");
        }
        
        // 删除车辆-驾驶员关联
        vehicleDriverMapper.deleteById(vehicleDriver.getId());
        
        // 查询该车辆剩余的驾驶员数量
        Integer remainingDriverCount = vehicleDriverMapper.countDriversByVehicleId(vehicleId);
        
        // 更新车辆状态
        if (remainingDriverCount == 0) {
            vehicle.setStatus(VehicleStatusConstant.AVAILABLE); // 可用
        } else {
            vehicle.setStatus(VehicleStatusConstant.OCCUPIED); // 已占用
        }
        vehicleMapper.update(vehicle);
        
        // 更新员工工作状态为空闲
        employee.setWorkStatus(0); // 空闲状态
        employeeMapper.update(employee);
    }
    
    /**
     * 获取车辆的所有驾驶员
     */
    @Override
    public List<Employee> getDriversByVehicleId(Long vehicleId) {
        return vehicleDriverMapper.getDriversByVehicleId(vehicleId);
    }
    
    /**
     * 获取员工分配的车辆
     */
    @Override
    public List<Vehicle> getVehiclesByEmployeeId(Long employeeId) {
        List<Vehicle> vehicles = vehicleDriverMapper.getVehiclesByEmployeeId(employeeId);
        
        // 后处理：确保车辆状态不为null，默认设为"已占用"
        for (Vehicle vehicle : vehicles) {
            if (vehicle.getStatus() == null) {
                // 获取完整的车辆信息，包括状态
                Vehicle fullVehicle = vehicleMapper.getById(vehicle.getVehicleId());
                if (fullVehicle != null && fullVehicle.getStatus() != null) {
                    vehicle.setStatus(fullVehicle.getStatus());
                } else {
                    // 如果仍然无法获取状态，设置默认值为"已占用"
                    vehicle.setStatus(VehicleStatusConstant.OCCUPIED); // 已占用
                }
            }
        }
        
        return vehicles;
    }
} 