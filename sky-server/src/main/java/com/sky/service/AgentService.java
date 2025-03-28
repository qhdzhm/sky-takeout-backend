package com.sky.service;

import com.sky.dto.AgentLoginDTO;
import com.sky.entity.Agent;

/**
 * 代理商Service接口
 */
public interface AgentService {

    /**
     * 代理商登录
     * @param agentLoginDTO 登录信息
     * @return 代理商对象
     */
    Agent login(AgentLoginDTO agentLoginDTO);

    /**
     * 获取当前登录代理商的折扣率
     * @return 折扣率
     */
    Double getDiscountRate();
} 