package com.sky.service;

import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.result.PageResult;

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
}
