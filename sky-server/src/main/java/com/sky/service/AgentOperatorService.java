package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.AgentOperator;

import java.util.List;

/**
 * 代理商操作员服务接口
 */
public interface AgentOperatorService {

    /**
     * 操作员登录
     * @param loginDTO 登录信息
     * @return 操作员信息
     */
    AgentOperator login(UserLoginDTO loginDTO);

    /**
     * 根据ID查询操作员
     * @param id 操作员ID
     * @return 操作员信息
     */
    AgentOperator getById(Long id);

    /**
     * 根据代理商ID查询操作员列表
     * @param agentId 代理商ID
     * @return 操作员列表
     */
    List<AgentOperator> getByAgentId(Long agentId);

    /**
     * 创建操作员
     * @param agentOperator 操作员信息
     */
    void create(AgentOperator agentOperator);

    /**
     * 更新操作员信息
     * @param agentOperator 操作员信息
     */
    void update(AgentOperator agentOperator);

    /**
     * 更新操作员状态
     * @param id 操作员ID
     * @param status 状态
     */
    void updateStatus(Long id, Integer status);

    /**
     * 删除操作员
     * @param id 操作员ID
     */
    void delete(Long id);
} 