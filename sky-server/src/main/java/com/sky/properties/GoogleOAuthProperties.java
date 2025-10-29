package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Google OAuth 配置属性
 */
@Component
@ConfigurationProperties(prefix = "google.oauth")
@Data
public class GoogleOAuthProperties {
    
    /**
     * Google OAuth 客户端 ID
     */
    private String clientId;
    
    /**
     * Google OAuth 客户端密钥
     */
    private String clientSecret;
    
    /**
     * 重定向 URI
     */
    private String redirectUri;
    
    /**
     * 授权范围
     */
    private String scope = "openid email profile";
    
    /**
     * Google 授权端点
     */
    private String authUri = "https://accounts.google.com/o/oauth2/v2/auth";
    
    /**
     * Google Token 端点
     */
    private String tokenUri = "https://oauth2.googleapis.com/token";
    
    /**
     * Google 用户信息端点
     */
    private String userInfoUri = "https://www.googleapis.com/oauth2/v3/userinfo";
}

