package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.JwtClaimsConstant;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.CustomerServiceDTO;
import com.sky.dto.CustomerServicePageQueryDTO;
import com.sky.dto.ServiceLoginDTO;
import com.sky.entity.CustomerService;
import com.sky.entity.Employee;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.EmployeeMapper;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.service.CustomerServiceService;
import com.sky.utils.JwtUtil;
import com.sky.vo.CustomerServiceStatisticsVO;
import com.sky.vo.CustomerServiceVO;
import com.sky.vo.ServiceLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 客服Service实现类
 */
@Service
@Slf4j
public class CustomerServiceServiceImpl implements CustomerServiceService {

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 客服登录
     */
    @Override
    public ServiceLoginVO login(ServiceLoginDTO serviceLoginDTO) {
        String username = serviceLoginDTO.getUsername();
        String password = serviceLoginDTO.getPassword();

        // 根据用户名查询员工
        Employee employee = employeeMapper.getByUsername(username);

        // 处理各种异常情况
        if (employee == null) {
            throw new LoginFailedException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 检查是否是客服角色
        if (employee.getRole() != 3) {
            throw new LoginFailedException("非客服账号，无法登录");
        }

        // 密码比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            throw new LoginFailedException(MessageConstant.PASSWORD_ERROR);
        }

        // 更新最后登录时间和在线状态
        employeeMapper.updateCustomerServiceOnlineStatus(employee.getId(), 1);

        // 生成JWT令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put("serviceId", employee.getId());
        claims.put(JwtClaimsConstant.USERNAME, employee.getUsername());
        String token = JwtUtil.createJWT(jwtProperties.getAdminSecretKey(), jwtProperties.getAdminTtl(), claims);

        ServiceLoginVO serviceLoginVO = ServiceLoginVO.builder()
                .id(employee.getId())
                .username(employee.getUsername())
                .name(employee.getName())
                .serviceNo(employee.getServiceNo())
                .token(token)
                .onlineStatus(1)
                .maxConcurrentCustomers(employee.getMaxConcurrentCustomers())
                .currentCustomerCount(employee.getCurrentCustomerCount())
                .serviceLevel(employee.getServiceLevel())
                .build();

        return serviceLoginVO;
    }

    /**
     * 客服登出
     */
    @Override
    public void logout(Long serviceId) {
        employeeMapper.updateCustomerServiceOnlineStatus(serviceId, 0);
    }

    /**
     * 更新客服在线状态
     */
    @Override
    public void updateOnlineStatus(Long serviceId, Integer status) {
        employeeMapper.updateCustomerServiceOnlineStatus(serviceId, status);
    }

    /**
     * 获取在线客服列表
     */
    @Override
    public List<CustomerService> getOnlineServices() {
        // 这个方法保持兼容性，但实际上应该使用新的方法
        return null;
    }

    /**
     * 根据技能获取合适的客服
     */
    @Override
    public CustomerService getAvailableService(String skillTag) {
        // 兼容性方法，使用新的Employee-based方法
        Employee employee = getAvailableEmployee(skillTag);
        if (employee != null) {
            CustomerService customerService = new CustomerService();
            BeanUtils.copyProperties(employee, customerService);
            return customerService;
        }
        return null;
    }

    /**
     * 更新客服当前服务客户数
     */
    @Override
    public void updateCurrentCustomerCount(Long serviceId, Integer count) {
        employeeMapper.updateCustomerServiceCurrentCount(serviceId, count);
    }

    // ===== 新增管理方法实现 =====

    /**
     * 分页查询客服列表
     */
    @Override
    public PageResult getServiceList(CustomerServicePageQueryDTO queryDTO) {
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getPageSize());
        Page<CustomerServiceVO> page = employeeMapper.customerServicePageQuery(queryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 获取客服统计信息
     */
    @Override
    public CustomerServiceStatisticsVO getStatistics() {
        return employeeMapper.getCustomerServiceStatistics();
    }

    /**
     * 创建客服
     */
    @Override
    public void createService(CustomerServiceDTO customerServiceDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(customerServiceDTO, employee);
        
        // 设置客服角色
        employee.setRole(3);
        employee.setWorkStatus(0); // 默认空闲
        employee.setOnlineStatus(0); // 默认离线
        employee.setCurrentCustomerCount(0); // 初始服务客户数为0
        
        // 设置创建和更新信息
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        
        // 生成客服工号
        if (employee.getServiceNo() == null || employee.getServiceNo().isEmpty()) {
            employee.setServiceNo("CS" + System.currentTimeMillis());
        }
        
        // 密码加密
        if (employee.getPassword() != null) {
            employee.setPassword(DigestUtils.md5DigestAsHex(employee.getPassword().getBytes()));
        }

        employeeMapper.insertCustomerService(employee);
    }

    /**
     * 更新客服信息
     */
    @Override
    public void updateService(Long id, CustomerServiceDTO customerServiceDTO) {
        Employee employee = employeeMapper.getCustomerServiceById(id);
        if (employee == null) {
            throw new RuntimeException("客服不存在");
        }

        BeanUtils.copyProperties(customerServiceDTO, employee);
        employee.setId(id);
        employee.setUpdateTime(LocalDateTime.now());
        
        // 如果有新密码，进行加密
        if (customerServiceDTO.getPassword() != null && !customerServiceDTO.getPassword().isEmpty()) {
            employee.setPassword(DigestUtils.md5DigestAsHex(customerServiceDTO.getPassword().getBytes()));
        }

        employeeMapper.updateCustomerService(employee);
    }

    /**
     * 根据ID获取客服详情
     */
    @Override
    public CustomerServiceVO getServiceDetail(Long id) {
        Employee employee = employeeMapper.getCustomerServiceById(id);
        if (employee == null) {
            return null;
        }

        CustomerServiceVO vo = new CustomerServiceVO();
        BeanUtils.copyProperties(employee, vo);
        vo.setEmployeeId(employee.getId());
        return vo;
    }

    /**
     * 禁用客服
     */
    @Override
    public void disableService(Long id) {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setWorkStatus(2); // 设置为休假状态
        employee.setOnlineStatus(0); // 设置为离线
        employee.setUpdateTime(LocalDateTime.now());
        employeeMapper.updateCustomerService(employee);
    }

    /**
     * 获取可用的客服员工（基于Employee表）
     */
    @Override
    public Employee getAvailableEmployee(String skillTag) {
        return employeeMapper.getAvailableCustomerService(skillTag);
    }
} 