package com.sky.service;

import com.sky.dto.EmployeeAssignDTO;
import com.sky.dto.PositionDTO;
import com.sky.vo.PositionVO;

import java.util.List;

/**
 * 职位管理Service接口
 */
public interface PositionService {

    /**
     * 获取所有职位列表
     * @return 职位列表
     */
    List<PositionVO> getAllPositions();

    /**
     * 根据ID获取职位信息
     * @param id 职位ID
     * @return 职位信息
     */
    PositionVO getPositionById(Long id);

    /**
     * 根据部门ID获取职位列表
     * @param deptId 部门ID
     * @return 职位列表
     */
    List<PositionVO> getPositionsByDeptId(Long deptId);

    /**
     * 创建职位
     * @param positionDTO 职位信息
     * @return 职位ID
     */
    Long createPosition(PositionDTO positionDTO);

    /**
     * 更新职位信息
     * @param positionDTO 职位信息
     */
    void updatePosition(PositionDTO positionDTO);

    /**
     * 删除职位
     * @param id 职位ID
     */
    void deletePosition(Long id);

    /**
     * 根据职位代码查询职位
     * @param positionCode 职位代码
     * @return 职位信息
     */
    PositionVO getPositionByCode(String positionCode);

    /**
     * 获取职位详细信息（包含统计数据）
     * @return 职位详细信息列表
     */
    List<PositionVO> getPositionDetails();

    /**
     * 分配员工到部门职位
     * @param employeeAssignDTO 员工分配信息
     */
    void assignEmployeeToPosition(EmployeeAssignDTO employeeAssignDTO);

    /**
     * 批量分配员工到部门职位
     * @param assignList 批量分配信息列表
     */
    void batchAssignEmployees(List<EmployeeAssignDTO> assignList);

    /**
     * 获取可分配的职位列表（排除已满编制的职位）
     * @param deptId 部门ID（可选）
     * @return 职位列表
     */
    List<PositionVO> getAvailablePositions(Long deptId);
}

