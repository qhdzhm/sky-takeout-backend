package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.VehicleStatusConstant;
import com.sky.dto.VehiclePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.entity.Vehicle;
import com.sky.entity.VehicleDriver;
import com.sky.mapper.EmployeeMapper;
import com.sky.mapper.VehicleDriverMapper;
import com.sky.mapper.VehicleMapper;
import com.sky.result.PageResult;
import com.sky.service.VehicleService;
import com.sky.vo.VehicleWithDriversVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class VehicleServiceImpl implements VehicleService {

    @Autowired
    private VehicleMapper vehicleMapper;
    
    @Autowired
    private VehicleDriverMapper vehicleDriverMapper;
    
    @Autowired
    private EmployeeMapper employeeMapper;

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
        
        // 为每个车辆添加驾驶员信息和更新状态
        List<Vehicle> filteredVehicles = new ArrayList<>();
        for (Vehicle vehicle : vehicles) {
            // 查询该车辆分配的驾驶员数量
            Integer currentDriverCount = vehicleDriverMapper.countDriversByVehicleId(vehicle.getVehicleId());
            vehicle.setCurrentDriverCount(currentDriverCount);
            
            // 获取该车辆的驾驶员列表
            List<VehicleDriver> drivers = vehicleDriverMapper.getByVehicleId(vehicle.getVehicleId());
            if (drivers != null && !drivers.isEmpty()) {
                // 提取驾驶员名称
                List<String> driverNames = new ArrayList<>();
                for (VehicleDriver driver : drivers) {
                    Employee employee = employeeMapper.getById(driver.getEmployeeId().intValue());
                    if (employee != null) {
                        driverNames.add(employee.getName());
                    }
                }
                vehicle.setDriverNames(driverNames);
            } else {
                // 如果没有驾驶员，初始化为空列表
                vehicle.setDriverNames(new ArrayList<>());
            }
            
            // 更新车辆状态
            updateVehicleStatus(vehicle, currentDriverCount, today);
            
            // 如果指定了状态过滤，只添加匹配的车辆
            if (vehiclePageQueryDTO.getStatus() == null || vehicle.getStatus().equals(vehiclePageQueryDTO.getStatus())) {
                filteredVehicles.add(vehicle);
            }
        }
        
        // 更新总数和过滤后的列表
        long total = filteredVehicles.size();
        if (vehiclePageQueryDTO.getStatus() != null) {
            page.setTotal(total);
        }
        
        return new PageResult(page.getTotal(), filteredVehicles);
    }

    /**
     * 更新车辆状态
     * 状态优先级：
     * 1. 送修状态（手动设置为0）
     * 2. 注册过期 (4)
     * 3. 车检过期 (5)
     * 4. 驾驶员已满 (3)
     * 5. 已占用 (2)
     * 6. 可用 (1)
     */
    private void updateVehicleStatus(Vehicle vehicle, Integer currentDriverCount, LocalDate today) {
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
        
        // 检查驾驶员分配情况
        if (currentDriverCount >= vehicle.getMaxDrivers()) {
            vehicle.setStatus(VehicleStatusConstant.FULL);
        } else if (currentDriverCount > 0) {
            vehicle.setStatus(VehicleStatusConstant.OCCUPIED);
        } else {
            vehicle.setStatus(VehicleStatusConstant.AVAILABLE);
        }
    }

    /**
     * 根据ID查询车辆
     */
    @Override
    public Vehicle getById(Long id) {
        Vehicle vehicle = vehicleMapper.getById(id);
        if (vehicle != null && vehicle.getStatus() == null) {
            // 如果状态为null，设置默认状态为OCCUPIED（已占用）
            vehicle.setStatus(VehicleStatusConstant.OCCUPIED);
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
    
    /**
     * 获取车辆及其驾驶员信息
     */
    @Override
    public VehicleWithDriversVO getVehicleWithDrivers(Long id) {
        // 获取车辆基本信息
        Vehicle vehicle = vehicleMapper.getById(id);
        if (vehicle == null) {
            throw new RuntimeException("车辆不存在");
        }
        
        // 创建返回对象
        VehicleWithDriversVO vo = new VehicleWithDriversVO();
        BeanUtils.copyProperties(vehicle, vo);
        
        // 获取分配给该车辆的驾驶员ID列表
        List<VehicleDriver> vehicleDrivers = vehicleDriverMapper.getByVehicleId(id);
        
        // 添加驾驶员信息
        List<Employee> drivers = new ArrayList<>();
        if (vehicleDrivers != null && !vehicleDrivers.isEmpty()) {
            for (VehicleDriver vehicleDriver : vehicleDrivers) {
                Employee employee = employeeMapper.getById(vehicleDriver.getEmployeeId().intValue());
                if (employee != null) {
                    // 使用setter方法设置isPrimary字段
                    employee.setIsPrimary(vehicleDriver.getIsPrimary());
                    drivers.add(employee);
                }
            }
        }
        
        // 设置驾驶员列表
        vo.setDrivers(drivers);
        
        // 设置当前驾驶员数量
        vo.setCurrentDriverCount(drivers.size());
        
        // 设置分配情况描述
        vo.setAllocation(drivers.size() + "/" + (vo.getMaxDrivers() != null ? vo.getMaxDrivers() : 3));
        
        // 设置是否已满
        vo.setIsFull(vo.getCurrentDriverCount() >= vo.getMaxDrivers());
        
        // 当前日期，用于比较到期日期
        LocalDate today = LocalDate.now();
        
        // 设置注册和车检状态
        vo.setIsRegoExpired(vehicle.getRegoExpiryDate() != null && today.isAfter(vehicle.getRegoExpiryDate()));
        vo.setIsInspectionExpired(vehicle.getInspectionDueDate() != null && today.isAfter(vehicle.getInspectionDueDate()));
        
        // 计算剩余天数
        if (vehicle.getRegoExpiryDate() != null) {
            long regoRemainingDays = ChronoUnit.DAYS.between(today, vehicle.getRegoExpiryDate());
            vo.setRegoRemainingDays(regoRemainingDays);
        }
        
        if (vehicle.getInspectionDueDate() != null) {
            long inspectionRemainingDays = ChronoUnit.DAYS.between(today, vehicle.getInspectionDueDate());
            vo.setInspectionRemainingDays(inspectionRemainingDays);
        }
        
        // 根据当前状态设置状态描述
        setStatusDescription(vo);
        
        return vo;
    }
    
    /**
     * 根据状态设置状态描述
     */
    private void setStatusDescription(VehicleWithDriversVO vo) {
        vo.setStatusDescription(VehicleStatusConstant.getStatusDesc(vo.getStatus()));
    }
    
    /**
     * 获取所有可分配车辆列表（包含驾驶员数量信息）
     */
    @Override
    public List<VehicleWithDriversVO> getAllAvailableVehicles() {
        // 查询所有状态为可用的车辆
        VehiclePageQueryDTO queryDTO = new VehiclePageQueryDTO();
        queryDTO.setStatus(1); // 1-可用
        queryDTO.setPage(1);
        queryDTO.setPageSize(1000); // 设置一个较大的数值，实际上相当于查询所有
        
        Page<Vehicle> page = vehicleMapper.pageQuery(queryDTO);
        List<Vehicle> vehicles = page.getResult();
        
        // 当前日期，用于比较到期日期
        LocalDate today = LocalDate.now();
        
        // 转换为VO列表并过滤掉不符合条件的车辆
        List<VehicleWithDriversVO> result = new ArrayList<>();
        for (Vehicle vehicle : vehicles) {
            // 如果注册或车检已过期，跳过此车辆
            if ((vehicle.getRegoExpiryDate() != null && today.isAfter(vehicle.getRegoExpiryDate())) || 
                (vehicle.getInspectionDueDate() != null && today.isAfter(vehicle.getInspectionDueDate()))) {
                continue;
            }
            
            VehicleWithDriversVO vo = new VehicleWithDriversVO();
            BeanUtils.copyProperties(vehicle, vo);
            
            // 获取驾驶员信息
            List<VehicleDriver> vehicleDrivers = vehicleDriverMapper.getByVehicleId(vehicle.getVehicleId());
            
            // 添加驾驶员信息
            List<Employee> drivers = new ArrayList<>();
            if (vehicleDrivers != null && !vehicleDrivers.isEmpty()) {
                for (VehicleDriver vehicleDriver : vehicleDrivers) {
                    Employee employee = employeeMapper.getById(vehicleDriver.getEmployeeId().intValue());
                    if (employee != null) {
                        // 设置是否为主驾驶
                        employee.setIsPrimary(vehicleDriver.getIsPrimary());
                        drivers.add(employee);
                    }
                }
            }
            
            // 设置驾驶员列表
            vo.setDrivers(drivers);
            
            // 设置当前驾驶员数量
            vo.setCurrentDriverCount(drivers.size());
            
            // 设置分配情况描述
            vo.setAllocation(drivers.size() + "/" + (vo.getMaxDrivers() != null ? vo.getMaxDrivers() : 3));
            
            // 计算剩余天数
            if (vehicle.getRegoExpiryDate() != null) {
                long regoRemainingDays = ChronoUnit.DAYS.between(today, vehicle.getRegoExpiryDate());
                vo.setRegoRemainingDays(regoRemainingDays);
            }
            
            if (vehicle.getInspectionDueDate() != null) {
                long inspectionRemainingDays = ChronoUnit.DAYS.between(today, vehicle.getInspectionDueDate());
                vo.setInspectionRemainingDays(inspectionRemainingDays);
            }
            
            // 设置是否已满
            boolean isFull = vo.getCurrentDriverCount() >= vo.getMaxDrivers();
            vo.setIsFull(isFull);
            
            // 如果驾驶员未满，才添加到可用列表
            if (!isFull) {
                result.add(vo);
            }
        }
        
        return result;
    }
}