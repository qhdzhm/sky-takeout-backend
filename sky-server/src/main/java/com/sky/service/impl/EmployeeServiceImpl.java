package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.StatusConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.EmployeeVehicleDTO;
import com.sky.entity.Employee;
import com.sky.entity.Vehicle;
import com.sky.entity.VehicleDriver;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.BusinessException;
import com.sky.mapper.EmployeeMapper;
import com.sky.mapper.VehicleDriverMapper;
import com.sky.mapper.VehicleMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import com.sky.service.VehicleService;
import com.sky.vo.VehicleWithDriversVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;
    
    @Autowired
    private VehicleDriverMapper vehicleDriverMapper;
    
    @Autowired
    private VehicleMapper vehicleMapper;
    
    @Autowired
    private VehicleService vehicleService;

    /**
     * 员工登录
     */
    @Override
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = DigestUtils.md5DigestAsHex(employeeLoginDTO.getPassword().getBytes());

        Employee employee = employeeMapper.getByUsername(username);
        if (employee == null || !password.equals(employee.getPassword())) {
            throw new AccountNotFoundException("账号或密码错误");
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            throw new AccountNotFoundException("账号已禁用");
        }

        return employee;
    }

    /**
     * 分页查询员工
     */
    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());
        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 启用/禁用员工
     */
    @Override
    public void status(Long id, Integer newStatus) {
        Employee employee = Employee.builder()
                .id(id)
                .status(newStatus)
                .build();
        employeeMapper.status(employee);
    }

    /**
     * 新增员工
     */
    @Override
    public void addEmp(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);

        // 设置初始密码
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        employee.setStatus(StatusConstant.ENABLE);

        employeeMapper.insert(employee);
    }

    /**
     * 根据ID查询员工
     */
    @Override
    public Employee getEmp(Integer id) {
        return employeeMapper.getById(id);
    }

    /**
     * 更新员工信息
     */
    @Override
    public void updateEmp(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
        employeeMapper.update(employee);
    }
    
    /**
     * 为员工分配车辆
     */
    @Override
    @Transactional
    public void assignVehicle(EmployeeVehicleDTO employeeVehicleDTO) {
        Long employeeId = employeeVehicleDTO.getEmployeeId();
        Long vehicleId = employeeVehicleDTO.getVehicleId();
        
        // 1. 检查员工是否已分配车辆
        List<VehicleDriver> existingAssignments = vehicleDriverMapper.getByEmployeeId(employeeId);
        if (existingAssignments != null && !existingAssignments.isEmpty()) {
            // 先取消现有分配，删除所有与该员工关联的记录
            for (VehicleDriver vd : existingAssignments) {
                vehicleDriverMapper.deleteById(vd.getId());
            }
        }
        
        // 2. 检查车辆当前司机数量是否已达到最大
        Vehicle vehicle = vehicleMapper.getById(vehicleId);
        if (vehicle == null) {
            throw new BusinessException("车辆不存在");
        }
        
        Integer currentDrivers = vehicleDriverMapper.countDriversByVehicleId(vehicleId);
        if (currentDrivers >= vehicle.getMaxDrivers()) {
            throw new BusinessException("车辆已达到最大司机数量");
        }
        
        // 3. 创建新的车辆-司机关系
        VehicleDriver vehicleDriver = VehicleDriver.builder()
                .vehicleId(vehicleId)
                .employeeId(employeeId)
                .isPrimary(employeeVehicleDTO.getIsPrimary())
                .build();
        
        vehicleDriverMapper.insert(vehicleDriver);
    }
    
    /**
     * 取消员工车辆分配
     */
    @Override
    @Transactional
    public void unassignVehicle(Long employeeId) {
        // 获取所有与该员工关联的车辆驾驶员记录
        List<VehicleDriver> assignments = vehicleDriverMapper.getByEmployeeId(employeeId);
        
        // 删除所有与该员工关联的记录
        if (assignments != null && !assignments.isEmpty()) {
            for (VehicleDriver vd : assignments) {
                vehicleDriverMapper.deleteById(vd.getId());
            }
        }
    }
    
    /**
     * 获取员工所分配的车辆
     */
    @Override
    public VehicleWithDriversVO getAssignedVehicle(Long employeeId) {
        // 查询员工是否已分配车辆
        List<VehicleDriver> vehicleDrivers = vehicleDriverMapper.getByEmployeeId(employeeId);
        if (vehicleDrivers == null || vehicleDrivers.isEmpty()) {
            return null;
        }
        
        // 获取第一个分配的车辆详情
        return vehicleService.getVehicleWithDrivers(vehicleDrivers.get(0).getVehicleId());
    }
}