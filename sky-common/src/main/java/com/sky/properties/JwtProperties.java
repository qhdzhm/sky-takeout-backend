package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sky.jwt")
@Data
public class JwtProperties {

    /**
     * 管理端员工生成jwt令牌相关配置
     */
    private String adminSecretKey;
    private long adminTtl;
    private String adminTokenName;

    /**
     * 用户端微信用户生成jwt令牌相关配置
     */
    private String userSecretKey;
    private long userTtl;
    private String userTokenName;
    
    /**
     * 代理商用户生成jwt令牌相关配置
     */
    private String agentSecretKey;
    private long agentTtl;
    private String agentTokenName;

    /**
     * Refresh Token相关配置
     */
    private long refreshTokenTtl = 7 * 24 * 60 * 60 * 1000L; // 默认7天
    
    /**
     * Access Token提前刷新时间（分钟）
     */
    private int tokenRefreshThreshold = 5; // 默认提前5分钟刷新
}
