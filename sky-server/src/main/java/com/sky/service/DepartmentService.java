package com.sky.service;

import com.sky.dto.DepartmentDTO;
import com.sky.vo.DepartmentVO;

import java.util.List;

/**
 * 部门管理Service接口
 */
public interface DepartmentService {

    /**
     * 获取所有部门列表
     * @return 部门列表
     */
    List<DepartmentVO> getAllDepartments();

    /**
     * 根据ID获取部门信息
     * @param id 部门ID
     * @return 部门信息
     */
    DepartmentVO getDepartmentById(Long id);

    /**
     * 创建部门
     * @param departmentDTO 部门信息
     * @return 部门ID
     */
    Long createDepartment(DepartmentDTO departmentDTO);

    /**
     * 更新部门信息
     * @param departmentDTO 部门信息
     */
    void updateDepartment(DepartmentDTO departmentDTO);

    /**
     * 删除部门
     * @param id 部门ID
     */
    void deleteDepartment(Long id);

    /**
     * 根据部门代码查询部门
     * @param deptCode 部门代码
     * @return 部门信息
     */
    DepartmentVO getDepartmentByCode(String deptCode);

    /**
     * 获取部门统计信息（包含职位数和员工数）
     * @return 部门统计列表
     */
    List<DepartmentVO> getDepartmentStatistics();
}

