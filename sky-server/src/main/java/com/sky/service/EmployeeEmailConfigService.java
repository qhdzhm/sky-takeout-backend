package com.sky.service;

import com.sky.dto.EmployeeEmailConfigDTO;
import com.sky.entity.Employee;

/**
 * 员工邮箱配置服务接口
 */
public interface EmployeeEmailConfigService {

    /**
     * 配置员工邮箱
     * @param configDTO 邮箱配置信息
     */
    void configEmployeeEmail(EmployeeEmailConfigDTO configDTO);

    /**
     * 获取员工邮箱配置
     * @param employeeId 员工ID
     * @return 员工信息（包含邮箱配置）
     */
    Employee getEmployeeEmailConfig(Long employeeId);

    /**
     * 测试员工邮箱连接
     * @param employeeId 员工ID
     * @return 测试结果
     */
    boolean testEmployeeEmailConnection(Long employeeId);

    /**
     * 启用/禁用员工邮箱发送
     * @param employeeId 员工ID
     * @param enabled 是否启用
     */
    void updateEmailEnabled(Long employeeId, Boolean enabled);

    /**
     * 加密邮箱密码
     * @param password 原始密码
     * @return 加密后的密码
     */
    String encryptPassword(String password);

    /**
     * 解密邮箱密码
     * @param encryptedPassword 加密的密码
     * @return 原始密码
     */
    String decryptPassword(String encryptedPassword);
}
