package com.sky.service;

import com.sky.dto.AgentDTO;
import com.sky.dto.AgentLoginDTO;
import com.sky.dto.AgentPageQueryDTO;
import com.sky.dto.AgentPasswordResetDTO;
import com.sky.dto.PasswordChangeDTO;
import com.sky.entity.Agent;
import com.sky.result.PageResult;
import com.sky.vo.AgentVO;
import com.sky.vo.AgentSimpleVO;

import java.util.List;

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
    
    /**
     * 分页查询代理商
     * @param agentPageQueryDTO 查询条件
     * @return 分页结果
     */
    PageResult pageQuery(AgentPageQueryDTO agentPageQueryDTO);
    
    /**
     * 新增代理商
     * @param agentDTO 代理商信息
     */
    void save(AgentDTO agentDTO);
    
    /**
     * 修改代理商
     * @param agentDTO 代理商信息
     */
    void update(AgentDTO agentDTO);
    
    /**
     * 根据ID获取代理商信息
     * @param id 代理商ID
     * @return 代理商信息
     */
    AgentVO getById(Long id);
    
    /**
     * 删除代理商
     * @param id 代理商ID
     */
    void deleteById(Long id);
    
    /**
     * 启用/禁用代理商账号
     * @param id 代理商ID
     * @param status 状态(0-禁用，1-启用)
     */
    void updateStatus(Long id, Integer status);
    
    /**
     * 重置代理商密码
     * @param agentPasswordResetDTO 重置密码信息
     */
    void resetPassword(AgentPasswordResetDTO agentPasswordResetDTO);

    /**
     * 获取代理商下拉选项列表（支持名称模糊搜索）
     * @param name 代理商名称关键字（可选）
     * @param id 代理商ID（可选，用于精确查询）
     * @return 代理商简略信息列表
     */
    List<AgentSimpleVO> getAgentOptions(String name, Long id);
    
    /**
     * 代理商修改自己的密码
     * @param agentId 代理商ID
     * @param passwordChangeDTO 密码修改信息
     */
    void changePassword(Long agentId, PasswordChangeDTO passwordChangeDTO);
    
    /**
     * 更新代理商折扣等级
     * @param agentId 代理商ID
     * @param discountLevelId 折扣等级ID
     */
    void updateDiscountLevel(Long agentId, Long discountLevelId);
    
    /**
     * 更新代理商头像
     * @param agentId 代理商ID
     * @param avatar 头像URL
     */
    void updateAvatar(Long agentId, String avatar);
} 