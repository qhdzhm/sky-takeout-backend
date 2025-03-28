package com.sky.service;

import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.EmployeeVehicleDTO;
import com.sky.entity.Employee;
import com.sky.result.PageResult;
import com.sky.vo.VehicleWithDriversVO;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    /**
     * 分页查询员工
     */
    PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 启用/禁用员工
     */
    void status(Long id, Integer newStatus);

    /**
     * 新增员工
     */
    void addEmp(EmployeeDTO employeeDTO);

    /**
     * 根据ID查询员工
     */
    Employee getEmp(Integer id);

    /**
     * 更新员工信息
     */
    void updateEmp(EmployeeDTO employeeDTO);
    
    /**
     * 为员工分配车辆
     */
    void assignVehicle(EmployeeVehicleDTO employeeVehicleDTO);
    
    /**
     * 取消员工车辆分配
     */
    void unassignVehicle(Long employeeId);
    
    /**
     * 获取员工所分配的车辆
     */
    VehicleWithDriversVO getAssignedVehicle(Long employeeId);
}
