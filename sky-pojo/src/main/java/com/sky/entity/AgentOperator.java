package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 代理商操作员实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentOperator {
    
    private Long id;
    
    /**
     * 所属代理商ID
     */
    private Long agentId;
    
    /**
     * 操作员用户名
     */
    private String username;
    
    /**
     * 密码
     */
    private String password;
    
    /**
     * 操作员姓名
     */
    private String name;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 电话
     */
    private String phone;
    
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 状态：0-禁用，1-正常
     */
    private Integer status;
    
    /**
     * 权限配置（JSON格式）
     */
    private String permissions;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
} 