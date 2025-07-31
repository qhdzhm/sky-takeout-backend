package com.sky.service;

import com.sky.dto.CustomerServiceDTO;
import com.sky.dto.CustomerServicePageQueryDTO;
import com.sky.dto.ServiceLoginDTO;
import com.sky.entity.CustomerService;
import com.sky.entity.Employee;
import com.sky.result.PageResult;
import com.sky.vo.CustomerServiceStatisticsVO;
import com.sky.vo.CustomerServiceVO;
import com.sky.vo.ServiceLoginVO;

import java.util.List;

/**
 * 客服Service接口
 */
public interface CustomerServiceService {

    /**
     * 客服登录
     */
    ServiceLoginVO login(ServiceLoginDTO serviceLoginDTO);

    /**
     * 客服登出
     */
    void logout(Long serviceId);

    /**
     * 更新客服在线状态
     */
    void updateOnlineStatus(Long serviceId, Integer status);

    /**
     * 获取在线客服列表
     */
    List<CustomerService> getOnlineServices();

    /**
     * 根据技能获取合适的客服
     */
    CustomerService getAvailableService(String skillTag);

    /**
     * 更新客服当前服务客户数
     */
    void updateCurrentCustomerCount(Long serviceId, Integer count);

    // ===== 新增管理方法 =====

    /**
     * 分页查询客服列表
     */
    PageResult getServiceList(CustomerServicePageQueryDTO queryDTO);

    /**
     * 获取客服统计信息
     */
    CustomerServiceStatisticsVO getStatistics();

    /**
     * 创建客服
     */
    void createService(CustomerServiceDTO customerServiceDTO);

    /**
     * 更新客服信息
     */
    void updateService(Long id, CustomerServiceDTO customerServiceDTO);

    /**
     * 根据ID获取客服详情
     */
    CustomerServiceVO getServiceDetail(Long id);

    /**
     * 禁用客服
     */
    void disableService(Long id);

    /**
     * 获取可用的客服员工（基于Employee表）
     */
    Employee getAvailableEmployee(String skillTag);
} 