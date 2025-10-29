package com.sky.service.impl;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.AgentOperator;
import com.sky.exception.BusinessException;
import com.sky.mapper.AgentOperatorMapper;
import com.sky.service.AgentOperatorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 代理商操作员服务实现类
 */
@Service
@Slf4j
public class AgentOperatorServiceImpl implements AgentOperatorService {

    @Autowired
    private AgentOperatorMapper agentOperatorMapper;

    @Override
    public AgentOperator login(UserLoginDTO loginDTO) {
        log.info("操作员登录尝试: {}", loginDTO.getUsername());
        
        // 根据用户名查找操作员
        AgentOperator operator = agentOperatorMapper.getByUsername(loginDTO.getUsername());
        if (operator == null) {
            log.info("操作员不存在: {}", loginDTO.getUsername());
            throw new BusinessException("用户名或密码错误");
        }
        
        // 验证密码 - 使用MD5加密验证
        String encryptedPassword = DigestUtils.md5DigestAsHex(loginDTO.getPassword().getBytes());
        if (!encryptedPassword.equals(operator.getPassword())) {
            log.info("操作员密码错误: {}", loginDTO.getUsername());
            throw new BusinessException("用户名或密码错误");
        }
        
        // 检查状态
        if (operator.getStatus() == null || operator.getStatus() != 1) {
            log.info("操作员账号已被禁用: {}", loginDTO.getUsername());
            throw new BusinessException("账号已被禁用");
        }
        
        log.info("操作员登录成功: {}, 所属代理商ID: {}", operator.getUsername(), operator.getAgentId());
        return operator;
    }

    @Override
    public AgentOperator getById(Long id) {
        return agentOperatorMapper.getById(id);
    }

    @Override
    public List<AgentOperator> getByAgentId(Long agentId) {
        return agentOperatorMapper.getByAgentId(agentId);
    }

    @Override
    public void create(AgentOperator agentOperator) {
        // 检查用户名是否已存在
        if (agentOperatorMapper.countByUsername(agentOperator.getUsername()) > 0) {
            throw new BusinessException("用户名已存在");
        }
        
        // 设置默认值
        if (agentOperator.getStatus() == null) {
            agentOperator.setStatus(1);
        }
        if (agentOperator.getName() == null) {
            agentOperator.setName("");
        }
        if (agentOperator.getEmail() == null) {
            agentOperator.setEmail("");
        }
        if (agentOperator.getPhone() == null) {
            agentOperator.setPhone("");
        }
        if (agentOperator.getPermissions() == null) {
            agentOperator.setPermissions("{}");
        }
        
        // 密码加密
        if (agentOperator.getPassword() != null) {
            agentOperator.setPassword(DigestUtils.md5DigestAsHex(agentOperator.getPassword().getBytes()));
        }
        
        agentOperator.setCreatedAt(LocalDateTime.now());
        agentOperator.setUpdatedAt(LocalDateTime.now());
        
        agentOperatorMapper.insert(agentOperator);
        log.info("创建操作员成功: {}", agentOperator.getUsername());
    }

    @Override
    public void update(AgentOperator agentOperator) {
        // 检查用户名是否已被其他操作员使用
        if (agentOperatorMapper.countByUsernameExcludeId(agentOperator.getUsername(), agentOperator.getId()) > 0) {
            throw new BusinessException("用户名已存在");
        }
        
        agentOperator.setUpdatedAt(LocalDateTime.now());
        agentOperatorMapper.update(agentOperator);
        log.info("更新操作员信息成功: {}", agentOperator.getUsername());
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        agentOperatorMapper.updateStatus(id, status, LocalDateTime.now());
        log.info("更新操作员状态成功: ID={}, 状态={}", id, status);
    }

    @Override
    public void delete(Long id) {
        agentOperatorMapper.deleteById(id);
        log.info("删除操作员成功: ID={}", id);
    }
    
    @Override
    public void changePassword(Long operatorId, com.sky.dto.PasswordChangeDTO passwordChangeDTO) {
        log.info("操作员修改密码：operatorId={}", operatorId);
        
        // 1. 检查操作员是否存在
        AgentOperator operator = agentOperatorMapper.getById(operatorId);
        if (operator == null) {
            throw new BusinessException("操作员不存在");
        }
        
        // 2. 验证旧密码
        String oldEncryptedPassword = DigestUtils.md5DigestAsHex(passwordChangeDTO.getOldPassword().getBytes());
        if (!oldEncryptedPassword.equals(operator.getPassword())) {
            throw new BusinessException("当前密码错误");
        }
        
        // 3. 验证新密码
        if (passwordChangeDTO.getNewPassword() == null || passwordChangeDTO.getNewPassword().trim().isEmpty()) {
            throw new BusinessException("新密码不能为空");
        }
        
        if (passwordChangeDTO.getNewPassword().length() < 6) {
            throw new BusinessException("密码长度不能少于6位");
        }
        
        // 4. 加密新密码
        String newEncryptedPassword = DigestUtils.md5DigestAsHex(passwordChangeDTO.getNewPassword().getBytes());
        
        // 5. 更新密码
        agentOperatorMapper.updatePassword(operatorId, newEncryptedPassword, LocalDateTime.now());
        
        log.info("操作员密码修改成功: operatorId={}", operatorId);
    }
    
    @Override
    public void resetPassword(Long operatorId, String newPassword) {
        log.info("重置操作员密码：operatorId={}", operatorId);
        
        // 1. 检查操作员是否存在
        AgentOperator operator = agentOperatorMapper.getById(operatorId);
        if (operator == null) {
            throw new BusinessException("操作员不存在");
        }
        
        // 2. 验证新密码
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new BusinessException("新密码不能为空");
        }
        
        if (newPassword.length() < 6) {
            throw new BusinessException("密码长度不能少于6位");
        }
        
        // 3. 加密密码
        String encryptedPassword = DigestUtils.md5DigestAsHex(newPassword.getBytes());
        
        // 4. 更新密码
        agentOperatorMapper.updatePassword(operatorId, encryptedPassword, LocalDateTime.now());
        
        log.info("操作员密码重置成功: operatorId={}", operatorId);
    }
} 