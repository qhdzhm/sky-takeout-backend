package com.sky.service;

import com.sky.dto.BatchPermissionDTO;
import com.sky.dto.PermissionAssignDTO;
import com.sky.vo.PermissionManagementVO;
import com.sky.vo.PositionPermissionVO;

/**
 * 权限管理Service接口
 */
public interface PermissionManagementService {

    /**
     * 获取权限管理概览数据
     * @return 权限管理概览
     */
    PermissionManagementVO getPermissionManagementOverview();

    /**
     * 获取指定职位的权限配置
     * @param positionId 职位ID
     * @return 职位权限配置
     */
    PositionPermissionVO getPositionPermissions(Long positionId);

    /**
     * 更新职位权限配置
     * @param permissionAssignDTO 权限分配信息
     */
    void updatePositionPermissions(PermissionAssignDTO permissionAssignDTO);

    /**
     * 批量权限操作
     * @param batchPermissionDTO 批量操作信息
     */
    void batchPermissionOperation(BatchPermissionDTO batchPermissionDTO);

    /**
     * 复制职位权限
     * @param sourcePositionId 源职位ID
     * @param targetPositionIds 目标职位ID列表
     * @param operatorId 操作员ID
     */
    void copyPositionPermissions(Long sourcePositionId, java.util.List<Long> targetPositionIds, Long operatorId);

    /**
     * 检查员工是否有页面访问权限
     * @param employeeId 员工ID
     * @param pagePath 页面路径
     * @return 是否有权限
     */
    Boolean checkEmployeePagePermission(Long employeeId, String pagePath);

    /**
     * 获取员工的所有页面权限
     * @param employeeId 员工ID
     * @return 页面路径列表
     */
    java.util.List<String> getEmployeePagePermissions(Long employeeId);
}

