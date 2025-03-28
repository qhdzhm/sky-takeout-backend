package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.AgentLoginDTO;
import com.sky.entity.Agent;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.AgentMapper;
import com.sky.service.AgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.math.BigDecimal;

/**
 * 代理商Service实现类
 */
@Service
public class AgentServiceImpl implements AgentService {

    @Autowired
    private AgentMapper agentMapper;

    /**
     * 代理商登录
     * @param agentLoginDTO 登录信息
     * @return 代理商对象
     */
    @Override
    public Agent login(AgentLoginDTO agentLoginDTO) {
        String username = agentLoginDTO.getUsername();
        String password = agentLoginDTO.getPassword();

        // 1. 根据用户名查询代理商
        Agent agent = agentMapper.getByUsername(username);
        
        // 2. 判断代理商是否存在
        if (agent == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        
        // 3. 密码比对，使用MD5加密
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(agent.getPassword())) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }
        
        // 4. 判断代理商状态
        if (agent.getStatus() == 0) {
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }
        
        // 5. 返回代理商对象
        return agent;
    }

    /**
     * 获取当前登录代理商的折扣率
     * @return 折扣率
     */
    @Override
    public Double getDiscountRate() {
        // 1. 获取当前登录代理商的ID
        Long agentId = BaseContext.getCurrentId();
        
        // 2. 根据ID查询代理商
        Agent agent = agentMapper.getById(agentId);
        
        // 3. 返回折扣率
        BigDecimal discountRate = agent != null ? agent.getDiscountRate() : BigDecimal.ONE;
        return discountRate.doubleValue();
    }
} 