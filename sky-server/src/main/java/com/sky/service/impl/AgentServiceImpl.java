package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.AgentDTO;
import com.sky.dto.AgentLoginDTO;
import com.sky.dto.AgentPageQueryDTO;
import com.sky.dto.AgentPasswordResetDTO;
import com.sky.dto.PasswordChangeDTO;
import com.sky.entity.Agent;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.BusinessException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.AgentMapper;
import com.sky.result.PageResult;
import com.sky.service.AgentService;
import com.sky.vo.AgentSimpleVO;
import com.sky.vo.AgentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 代理商Service实现类
 */
@Service
@Slf4j
@Transactional
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

    /**
     * 分页查询代理商
     * @param agentPageQueryDTO 查询条件
     * @return 分页结果
     */
    @Override
    public PageResult pageQuery(AgentPageQueryDTO agentPageQueryDTO) {
        PageHelper.startPage(agentPageQueryDTO.getPage(), agentPageQueryDTO.getPageSize());
        Page<Agent> page = agentMapper.pageQuery(agentPageQueryDTO);
        
        // 将Agent转换为AgentVO
        List<AgentVO> agentVOList = page.getResult().stream().map(agent -> {
            AgentVO agentVO = new AgentVO();
            BeanUtils.copyProperties(agent, agentVO);
            return agentVO;
        }).collect(Collectors.toList());
        
        return new PageResult(page.getTotal(), agentVOList);
    }

    /**
     * 新增代理商
     * @param agentDTO 代理商信息
     */
    @Override
    public void save(AgentDTO agentDTO) {
        // 1. 判断用户名是否已存在
        Agent existAgent = agentMapper.getByUsername(agentDTO.getUsername());
        if(existAgent != null) {
            throw new BusinessException("用户名已存在");
        }
        
        // 2. 创建Agent对象并设置属性
        Agent agent = new Agent();
        BeanUtils.copyProperties(agentDTO, agent);
        
        // 3. 设置密码加密
        String password = DigestUtils.md5DigestAsHex(agentDTO.getPassword().getBytes());
        agent.setPassword(password);
        
        // 4. 设置默认值
        agent.setStatus(agentDTO.getStatus() != null ? agentDTO.getStatus() : 1);
        agent.setDiscountRate(agentDTO.getDiscountRate() != null ? agentDTO.getDiscountRate() : BigDecimal.ONE);
        
        // 5. 保存代理商
        agentMapper.insert(agent);
    }

    /**
     * 修改代理商
     * @param agentDTO 代理商信息
     */
    @Override
    public void update(AgentDTO agentDTO) {
        // 1. 检查代理商是否存在
        Agent existAgent = agentMapper.getById(agentDTO.getId());
        if(existAgent == null) {
            throw new BusinessException("代理商不存在");
        }
        
        // 2. 创建Agent对象并设置属性
        Agent agent = new Agent();
        BeanUtils.copyProperties(agentDTO, agent);
        
        // 3. 更新代理商
        agentMapper.update(agent);
    }

    /**
     * 根据ID获取代理商信息
     * @param id 代理商ID
     * @return 代理商信息
     */
    @Override
    public AgentVO getById(Long id) {
        // 1. 根据ID查询代理商
        Agent agent = agentMapper.getById(id);
        if(agent == null) {
            throw new BusinessException("代理商不存在");
        }
        
        // 2. 转换为AgentVO对象
        AgentVO agentVO = new AgentVO();
        BeanUtils.copyProperties(agent, agentVO);
        
        return agentVO;
    }

    /**
     * 删除代理商
     * @param id 代理商ID
     */
    @Override
    public void deleteById(Long id) {
        // 1. 检查代理商是否存在
        Agent agent = agentMapper.getById(id);
        if(agent == null) {
            throw new BusinessException("代理商不存在");
        }
        
        // 2. 删除代理商
        agentMapper.deleteById(id);
    }

    /**
     * 启用/禁用代理商账号
     * @param id 代理商ID
     * @param status 状态(0-禁用，1-启用)
     */
    @Override
    public void updateStatus(Long id, Integer status) {
        // 1. 检查代理商是否存在
        Agent agent = agentMapper.getById(id);
        if(agent == null) {
            throw new BusinessException("代理商不存在");
        }
        
        // 2. 更新状态
        agentMapper.updateStatus(id, status);
    }

    /**
     * 重置代理商密码
     * @param agentPasswordResetDTO 重置密码信息
     */
    @Override
    public void resetPassword(AgentPasswordResetDTO agentPasswordResetDTO) {
        // 1. 检查代理商是否存在
        Agent agent = agentMapper.getById(agentPasswordResetDTO.getId());
        if(agent == null) {
            throw new BusinessException("代理商不存在");
        }
        
        // 2. 加密密码
        String encryptedPassword = DigestUtils.md5DigestAsHex(agentPasswordResetDTO.getNewPassword().getBytes());
        
        // 3. 更新密码
        agentMapper.resetPassword(agentPasswordResetDTO.getId(), encryptedPassword);
    }

    /**
     * 获取代理商下拉选项列表（支持名称模糊搜索）
     * @param name 代理商名称关键字（可选）
     * @param id 代理商ID（可选，用于精确查询）
     * @return 代理商简略信息列表
     */
    @Override
    public List<AgentSimpleVO> getAgentOptions(String name, Long id) {
        // 如果指定了ID，则直接通过ID查询
        if (id != null) {
            Agent agent = agentMapper.getById(id);
            if (agent != null) {
                AgentSimpleVO vo = AgentSimpleVO.builder()
                        .id(agent.getId())
                        .companyName(agent.getCompanyName())
                        .name(agent.getCompanyName())
                        .contactPerson(agent.getContactPerson())
                        .contactPhone(agent.getPhone())
                        .build();
                return Collections.singletonList(vo);
            }
            return Collections.emptyList();
        }
        
        // 否则通过名称关键字模糊查询
        List<Agent> agents = agentMapper.getAgentsByNameKeyword(name);
        if (agents == null || agents.isEmpty()) {
            return Collections.emptyList();
        }
        
        return agents.stream()
                .map(agent -> AgentSimpleVO.builder()
                        .id(agent.getId())
                        .companyName(agent.getCompanyName())
                        .name(agent.getCompanyName())
                        .contactPerson(agent.getContactPerson())
                        .contactPhone(agent.getPhone())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 代理商修改自己的密码
     * @param agentId 代理商ID
     * @param passwordChangeDTO 密码修改信息
     */
    @Override
    public void changePassword(Long agentId, PasswordChangeDTO passwordChangeDTO) {
        log.info("代理商修改密码：agentId={}", agentId);
        
        // 1. 验证代理商是否存在
        Agent agent = agentMapper.getById(agentId);
        if (agent == null) {
            throw new BusinessException("代理商不存在");
        }
        
        // 2. 验证旧密码是否正确
        String oldPassword = DigestUtils.md5DigestAsHex(passwordChangeDTO.getOldPassword().getBytes());
        if (!oldPassword.equals(agent.getPassword())) {
            throw new BusinessException("旧密码不正确");
        }
        
        // 3. 验证新密码有效性
        String newPassword = passwordChangeDTO.getNewPassword();
        if (newPassword == null || newPassword.length() < 6) {
            throw new BusinessException("新密码不能少于6位");
        }
        
        // 4. 加密并更新密码
        String encryptedPassword = DigestUtils.md5DigestAsHex(newPassword.getBytes());
        agentMapper.resetPassword(agentId, encryptedPassword);
    }

    /**
     * 更新代理商折扣等级
     * @param agentId 代理商ID
     * @param discountLevelId 折扣等级ID
     */
    @Override
    public void updateDiscountLevel(Long agentId, Long discountLevelId) {
        log.info("更新代理商折扣等级：agentId={}, discountLevelId={}", agentId, discountLevelId);
        
        // 1. 验证代理商是否存在
        Agent agent = agentMapper.getById(agentId);
        if (agent == null) {
            throw new BusinessException("代理商不存在");
        }
        
        // 2. 更新折扣等级ID
        agentMapper.updateDiscountLevel(agentId, discountLevelId);
    }
} 