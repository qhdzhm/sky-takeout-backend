package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.sky.constant.StatusConstant;
import com.sky.entity.User;
import com.sky.exception.BusinessException;
import com.sky.mapper.UserMapper;
import com.sky.properties.GoogleOAuthProperties;
import com.sky.service.GoogleOAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * Google OAuth 服务实现
 */
@Service
@Slf4j
public class GoogleOAuthServiceImpl implements GoogleOAuthService {
    
    @Autowired
    private GoogleOAuthProperties googleOAuthProperties;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private RestTemplate restTemplate;
    
    /**
     * 验证 Google ID Token 并获取用户信息
     */
    @Override
    public User verifyGoogleToken(String idToken) {
        try {
            log.info("🔍 开始验证 Google ID Token");
            log.info("📝 Token 长度: {}", idToken != null ? idToken.length() : 0);
            log.info("🔑 使用的 Client ID: {}", googleOAuthProperties.getClientId());
            
            // 创建 Google ID Token 验证器
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), 
                GsonFactory.getDefaultInstance()
            )
            .setAudience(Collections.singletonList(googleOAuthProperties.getClientId()))
            .build();
            
            log.info("🔐 开始执行 Token 验证...");
            
            // 验证 Token
            GoogleIdToken googleIdToken = verifier.verify(idToken);
            
            if (googleIdToken == null) {
                log.error("❌ Google ID Token 验证失败：Token 无效");
                log.error("⚠️ 可能的原因: 1) Token已过期 2) Client ID不匹配 3) Token签名无效");
                throw new BusinessException("Google 登录验证失败");
            }
            
            // 获取用户信息
            Payload payload = googleIdToken.getPayload();
            
            String googleId = payload.getSubject(); // Google 用户唯一 ID
            String email = payload.getEmail();
            Boolean emailVerified = payload.getEmailVerified();
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");
            
            log.info("✅ Google Token 验证成功");
            log.info("📧 Google Email: {}", email);
            log.info("👤 Google Name: {}", name);
            log.info("🆔 Google ID: {}", googleId);
            log.info("✓ Email Verified: {}", emailVerified);
            
            // 邮箱未验证的不允许登录
            if (!Boolean.TRUE.equals(emailVerified)) {
                log.error("❌ Google 邮箱未验证");
                throw new BusinessException("Google 邮箱未验证，请先验证邮箱");
            }
            
            // 查询或创建用户
            User user = findOrCreateUser(googleId, email, name, pictureUrl);
            
            return user;
            
        } catch (BusinessException e) {
            // 重新抛出业务异常
            throw e;
        } catch (Exception e) {
            log.error("❌ 验证 Google Token 失败", e);
            log.error("❌ 异常类型: {}", e.getClass().getName());
            log.error("❌ 异常消息: {}", e.getMessage());
            if (e.getCause() != null) {
                log.error("❌ 根本原因: {}", e.getCause().getMessage());
            }
            throw new BusinessException("Google 登录失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询或创建用户
     */
    private User findOrCreateUser(String googleId, String email, String name, String pictureUrl) {
        // 1. 先通过 google_id 查询
        User user = userMapper.getUserByGoogleId(googleId);
        
        if (user != null) {
            log.info("🔍 找到已存在的 Google 用户: {}", user.getUsername());
            
            // 更新 Google 信息
            user.setGoogleEmail(email);
            user.setGoogleName(name);
            user.setGoogleAvatar(pictureUrl);
            user.setGoogleLastLogin(LocalDateTime.now());
            userMapper.updateUser(user);
            
            log.info("✅ 更新用户 Google 信息成功");
            return user;
        }
        
        // 2. 通过邮箱查询是否有已注册用户
        user = userMapper.getUserByEmail(email);
        
        if (user != null) {
            log.info("🔗 找到邮箱匹配的用户，绑定 Google 账号: {}", email);
            
            // 绑定 Google 账号到现有用户
            user.setGoogleId(googleId);
            user.setGoogleEmail(email);
            user.setGoogleName(name);
            user.setGoogleAvatar(pictureUrl);
            user.setGoogleLastLogin(LocalDateTime.now());
            userMapper.updateUser(user);
            
            log.info("✅ Google 账号绑定成功");
            return user;
        }
        
        // 3. 创建新用户
        log.info("➕ 创建新的 Google 用户");
        
        // 为 Google 用户生成一个随机密码（他们不需要使用，但数据库字段不允许为空）
        String randomPassword = java.util.UUID.randomUUID().toString();
        
        // 拆分 Google 名字为 first_name 和 last_name
        String firstName = name;
        String lastName = "";
        if (name != null && name.contains(" ")) {
            String[] nameParts = name.split(" ", 2);
            firstName = nameParts[0];
            lastName = nameParts[1];
        }
        
        user = User.builder()
            .googleId(googleId)
            .googleEmail(email)
            .googleName(name)
            .googleAvatar(pictureUrl)
            .googleLastLogin(LocalDateTime.now())
            .username(email) // 使用邮箱作为用户名
            .email(email)
            .name(name)
            .firstName(firstName)  // 名
            .lastName(lastName)    // 姓
            .avatar(pictureUrl)
            .password(randomPassword) // 随机密码，Google 用户不会使用
            .userType("regular") // 普通用户
            .status(StatusConstant.ENABLE) // 启用状态
            .createTime(LocalDateTime.now())
            .updateTime(LocalDateTime.now())
            .build();
        
        userMapper.addUser(user);
        
        log.info("✅ 新 Google 用户创建成功，ID: {}", user.getId());
        
        return user;
    }
    
    /**
     * 生成 Google OAuth 授权 URL
     */
    @Override
    public String generateAuthUrl(String state) {
        return UriComponentsBuilder
            .fromUriString(googleOAuthProperties.getAuthUri())
            .queryParam("client_id", googleOAuthProperties.getClientId())
            .queryParam("redirect_uri", googleOAuthProperties.getRedirectUri())
            .queryParam("response_type", "code")
            .queryParam("scope", googleOAuthProperties.getScope())
            .queryParam("state", state)
            .queryParam("access_type", "offline")
            .queryParam("prompt", "consent")
            .build()
            .toUriString();
    }
    
    /**
     * 通过授权码获取访问令牌
     */
    @Override
    public String getAccessToken(String code) {
        try {
            log.info("🔑 使用授权码获取 Access Token");
            
            // 构建请求参数
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("code", code);
            params.add("client_id", googleOAuthProperties.getClientId());
            params.add("client_secret", googleOAuthProperties.getClientSecret());
            params.add("redirect_uri", googleOAuthProperties.getRedirectUri());
            params.add("grant_type", "authorization_code");
            
            // 发送 POST 请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                googleOAuthProperties.getTokenUri(),
                HttpMethod.POST,
                requestEntity,
                String.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JSONObject jsonObject = JSON.parseObject(response.getBody());
                String accessToken = jsonObject.getString("access_token");
                
                log.info("✅ 成功获取 Access Token");
                return accessToken;
            } else {
                log.error("❌ 获取 Access Token 失败，状态码: {}", response.getStatusCode());
                throw new BusinessException("获取 Google Access Token 失败");
            }
            
        } catch (Exception e) {
            log.error("❌ 获取 Access Token 失败", e);
            throw new BusinessException("获取 Google Access Token 失败: " + e.getMessage());
        }
    }
    
    /**
     * 通过访问令牌获取用户信息
     */
    @Override
    public User getUserInfo(String accessToken) {
        try {
            log.info("👤 使用 Access Token 获取用户信息");
            
            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
            
            // 发送 GET 请求
            ResponseEntity<String> response = restTemplate.exchange(
                googleOAuthProperties.getUserInfoUri(),
                HttpMethod.GET,
                requestEntity,
                String.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JSONObject userInfo = JSON.parseObject(response.getBody());
                
                String googleId = userInfo.getString("sub");
                String email = userInfo.getString("email");
                Boolean emailVerified = userInfo.getBoolean("email_verified");
                String name = userInfo.getString("name");
                String picture = userInfo.getString("picture");
                
                log.info("✅ 成功获取 Google 用户信息");
                log.info("📧 Email: {}", email);
                log.info("👤 Name: {}", name);
                
                // 邮箱未验证的不允许登录
                if (!Boolean.TRUE.equals(emailVerified)) {
                    log.error("❌ Google 邮箱未验证");
                    throw new BusinessException("Google 邮箱未验证，请先验证邮箱");
                }
                
                // 查询或创建用户
                User user = findOrCreateUser(googleId, email, name, picture);
                
                return user;
            } else {
                log.error("❌ 获取用户信息失败，状态码: {}", response.getStatusCode());
                throw new BusinessException("获取 Google 用户信息失败");
            }
            
        } catch (Exception e) {
            log.error("❌ 获取用户信息失败", e);
            throw new BusinessException("获取 Google 用户信息失败: " + e.getMessage());
        }
    }
}

