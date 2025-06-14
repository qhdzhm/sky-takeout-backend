package com.sky.controller.user;

import com.sky.config.SecurityConfig;
import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.RefreshTokenDTO;
import com.sky.entity.User;
import com.sky.properties.JwtProperties;
import com.sky.result.Result;
import com.sky.service.UserService;
import com.sky.utils.JwtUtil;
import com.sky.vo.TokenRefreshVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证相关接口 - 支持CSRF Token和安全登出
 */
@RestController
@RequestMapping("/auth")
@Slf4j
@Api(tags = "认证相关接口")
// CORS现在由Nginx处理，移除@CrossOrigin注解
public class AuthController {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private UserService userService;

    /**
     * 获取CSRF Token
     */
    @GetMapping("/csrf-token")
    @ApiOperation("获取CSRF Token")
    public Result<Map<String, String>> getCsrfToken(HttpServletRequest request) {
        log.info("生成CSRF Token");
        
        // 生成CSRF Token
        String csrfToken = SecurityConfig.generateCsrfToken();
        
        Map<String, String> response = new HashMap<>();
        response.put("csrfToken", csrfToken);
        
        log.info("CSRF Token生成成功");
        return Result.success(response);
    }
    
    /**
     * 安全登出 - 清除HttpOnly Cookie
     */
    @PostMapping("/logout")
    @ApiOperation("安全登出")
    public Result<String> logout(HttpServletResponse response, HttpServletRequest request) {
        log.info("执行安全登出");
        
        try {
            // 记录请求来源信息
            String userAgent = request.getHeader("User-Agent");
            String clientIp = request.getRemoteAddr();
            log.info("登出请求 - IP: {}, User-Agent: {}", clientIp, userAgent);
            
            // 清除认证Cookie - 使用更强的清理策略
            clearCookieMultipleWays(response, "authToken");
            clearCookieMultipleWays(response, "userInfo");
            clearCookieMultipleWays(response, "refreshToken");
            
            // 额外清理可能存在的其他认证Cookie
            String[] cookiesToClear = {
                "token", "jwt", "session", "auth", "login",
                "agentToken", "operatorToken", "userToken"
            };
            
            for (String cookieName : cookiesToClear) {
                clearCookieMultipleWays(response, cookieName);
            }
            
            log.info("安全登出成功 - 已清理所有认证Cookie");
            return Result.success("登出成功");
        } catch (Exception e) {
            log.error("安全登出失败", e);
            return Result.error("登出失败：" + e.getMessage());
        }
    }
    
    /**
     * 使用多种方式清理Cookie，确保跨平台兼容性
     */
    private void clearCookieMultipleWays(HttpServletResponse response, String cookieName) {
        try {
            // 方式1：标准清理
            SecurityConfig.clearSecureCookie(response, cookieName);
            
            // 方式2：设置多个域名变体
            String[] domains = {"localhost", "127.0.0.1", ".localhost", ".127.0.0.1"};
            String[] paths = {"/", "/api", "/agent", "/user"};
            
            for (String domain : domains) {
                for (String path : paths) {
                    String cookieValue = String.format("%s=; Path=%s; Domain=%s; Expires=Thu, 01 Jan 1970 00:00:00 GMT; Max-Age=0", 
                        cookieName, path, domain);
                    response.addHeader("Set-Cookie", cookieValue);
                    
                    // 同时设置HttpOnly和Secure变体
                    String secureCookieValue = String.format("%s=; Path=%s; Domain=%s; Expires=Thu, 01 Jan 1970 00:00:00 GMT; Max-Age=0; HttpOnly", 
                        cookieName, path, domain);
                    response.addHeader("Set-Cookie", secureCookieValue);
                }
            }
            
            log.debug("已清理Cookie: {}", cookieName);
        } catch (Exception e) {
            log.warn("清理Cookie失败: {} - {}", cookieName, e.getMessage());
        }
    }

    /**
     * 刷新Token - 使用Refresh Token获取新的Access Token
     */
    @PostMapping("/refresh")
    @ApiOperation("刷新Token")
    public Result<TokenRefreshVO> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        log.info("开始刷新Token");
        
        try {
            // 从HttpOnly Cookie中获取refresh token
            String refreshToken = SecurityConfig.getCookieValue(request, "refreshToken");
            
            if (refreshToken == null || refreshToken.isEmpty()) {
                log.warn("Refresh Token为空");
                return Result.error("Refresh Token不存在，请重新登录");
            }

            // 验证refresh token的有效性
            if (!JwtUtil.isRefreshTokenValid(jwtProperties.getUserSecretKey(), refreshToken)) {
                log.warn("Refresh Token无效或已过期");
                // 清除无效的refresh token cookie
                SecurityConfig.clearRefreshTokenCookie(response);
                return Result.error("Refresh Token无效或已过期，请重新登录");
            }

            // 从refresh token中提取用户信息
            Long userId = JwtUtil.extractUserId(jwtProperties.getUserSecretKey(), refreshToken);
            String username = JwtUtil.extractUsername(jwtProperties.getUserSecretKey(), refreshToken);
            String userType = JwtUtil.extractUserType(jwtProperties.getUserSecretKey(), refreshToken);

            if (userId == null || username == null) {
                log.warn("无法从Refresh Token中提取用户信息");
                return Result.error("Token信息不完整，请重新登录");
            }

            // 根据用户类型验证用户是否仍然存在且有效
            User user = null;
            String displayName = username;
            
            if ("regular".equals(userType)) {
                user = userService.getById(userId);
                if (user == null) {
                    log.warn("普通用户不存在: {}", userId);
                    SecurityConfig.clearRefreshTokenCookie(response);
                    return Result.error("用户不存在，请重新登录");
                }
                displayName = user.getName();
            } else if ("agent".equals(userType) || "agent_operator".equals(userType)) {
                // 对于代理商用户，需要验证代理商或操作员是否存在
                // 这里简化处理，实际应该根据userType调用相应的service
                log.info("代理商用户Token刷新: {}, 类型: {}", username, userType);
                displayName = username; // 代理商用户使用用户名作为显示名
            } else {
                log.warn("未知用户类型: {}", userType);
                SecurityConfig.clearRefreshTokenCookie(response);
                return Result.error("用户类型无效，请重新登录");
            }

            // 生成新的access token
            Map<String, Object> claims = new HashMap<>();
            claims.put(JwtClaimsConstant.USER_ID, userId);
            claims.put(JwtClaimsConstant.USERNAME, username);
            claims.put(JwtClaimsConstant.USER_TYPE, userType != null ? userType : "regular");

            String newAccessToken = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(), 
                jwtProperties.getUserTtl(), 
                claims
            );

            // 检查refresh token是否即将过期（提前1天刷新）
            boolean refreshTokenExpiringSoon = JwtUtil.isTokenExpiringSoon(
                jwtProperties.getUserSecretKey(), 
                refreshToken, 
                24 * 60 // 提前24小时
            );

            String newRefreshToken = refreshToken;
            boolean refreshTokenUpdated = false;

            // 如果refresh token即将过期，生成新的refresh token
            if (refreshTokenExpiringSoon) {
                Map<String, Object> refreshClaims = new HashMap<>();
                refreshClaims.put(JwtClaimsConstant.USER_ID, userId);
                refreshClaims.put(JwtClaimsConstant.USERNAME, username);
                refreshClaims.put(JwtClaimsConstant.USER_TYPE, userType != null ? userType : "regular");

                newRefreshToken = JwtUtil.createRefreshJWT(
                    jwtProperties.getUserSecretKey(),
                    jwtProperties.getRefreshTokenTtl(),
                    refreshClaims
                );

                // 设置新的refresh token cookie
                SecurityConfig.setRefreshTokenCookie(response, newRefreshToken, 
                    (int) (jwtProperties.getRefreshTokenTtl() / 1000));
                
                refreshTokenUpdated = true;
                log.info("Refresh Token已更新，用户: {}", username);
            }

            // 设置新的access token cookie
            SecurityConfig.setSecureCookie(response, "authToken", newAccessToken, 
                (int) (jwtProperties.getUserTtl() / 1000));

            // 更新用户信息cookie
            String userInfo;
            if ("regular".equals(userType)) {
                userInfo = String.format("{\"id\":%d,\"username\":\"%s\",\"userType\":\"%s\",\"name\":\"%s\"}", 
                    user.getId(), user.getUsername(), userType, user.getName());
            } else {
                userInfo = String.format("{\"id\":%d,\"username\":\"%s\",\"userType\":\"%s\",\"name\":\"%s\"}", 
                    userId, username, userType, displayName);
            }
            SecurityConfig.setRegularCookie(response, "userInfo", userInfo, 
                (int) (jwtProperties.getUserTtl() / 1000));

            // 构建响应
            TokenRefreshVO tokenRefreshVO = TokenRefreshVO.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshTokenUpdated ? newRefreshToken : null) // 只有更新时才返回
                    .userId(userId)
                    .username(username)
                    .userType(userType != null ? userType : "regular")
                    .accessTokenExpiry(System.currentTimeMillis() + jwtProperties.getUserTtl())
                    .refreshTokenExpiry(System.currentTimeMillis() + jwtProperties.getRefreshTokenTtl())
                    .refreshTokenUpdated(refreshTokenUpdated)
                    .build();

            log.info("Token刷新成功，用户: {}, Refresh Token更新: {}", username, refreshTokenUpdated);
            return Result.success(tokenRefreshVO);

        } catch (Exception e) {
            log.error("Token刷新失败", e);
            // 清除可能有问题的cookies
            SecurityConfig.clearRefreshTokenCookie(response);
            SecurityConfig.clearSecureCookie(response, "authToken");
            return Result.error("Token刷新失败：" + e.getMessage());
        }
    }
} 
 