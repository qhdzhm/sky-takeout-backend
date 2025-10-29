package com.sky.service;

import com.sky.entity.User;

/**
 * Google OAuth 服务接口
 */
public interface GoogleOAuthService {
    
    /**
     * 验证 Google ID Token 并获取用户信息
     * 
     * @param idToken Google 返回的 ID Token
     * @return 用户信息
     */
    User verifyGoogleToken(String idToken);
    
    /**
     * 生成 Google OAuth 授权 URL
     * 
     * @param state 状态参数，用于防止 CSRF 攻击
     * @return 授权 URL
     */
    String generateAuthUrl(String state);
    
    /**
     * 通过授权码获取访问令牌
     * 
     * @param code 授权码
     * @return 访问令牌
     */
    String getAccessToken(String code);
    
    /**
     * 通过访问令牌获取用户信息
     * 
     * @param accessToken 访问令牌
     * @return 用户信息
     */
    User getUserInfo(String accessToken);
}

