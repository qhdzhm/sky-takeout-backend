package com.sky.service;

import com.sky.entity.Employee;
import com.sky.vo.OperatorStatisticsVO;

import java.util.List;

/**
 * 员工角色职责管理服务接口
 */
public interface EmployeeRoleService {

    /**
     * 更新员工操作员类型
     * @param employeeId 员工ID
     * @param operatorType 操作员类型
     */
    void updateOperatorType(Long employeeId, String operatorType);


    /**
     * 批量更新员工分配权限
     * @param employeeIds 员工ID列表
     * @param canAssignOrders 是否有分配权限
     */
    void batchUpdateAssignPermission(List<Long> employeeIds, Boolean canAssignOrders);

    /**
     * 获取操作员统计信息
     * @return 操作员统计信息列表
     */
    List<OperatorStatisticsVO> getOperatorStatistics();


    /**
     * 获取酒店操作员列表
     * @return 酒店操作员列表
     */
    List<Employee> getHotelOperators();

    /**
     * 重置员工职责（设为普通员工）
     * @param employeeId 员工ID
     */
    void resetEmployeeRole(Long employeeId);

    /**
     * 获取员工角色变更历史
     * @param employeeId 员工ID
     * @return 变更历史列表
     */
    List<Object> getRoleChangeHistory(Long employeeId);

}

