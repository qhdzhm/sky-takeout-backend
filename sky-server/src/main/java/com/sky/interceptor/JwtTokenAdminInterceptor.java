package com.sky.interceptor;

import com.sky.constant.JwtClaimsConstant;
import com.sky.context.BaseContext;
import com.sky.properties.JwtProperties;
import com.sky.utils.JwtUtil;
import com.sky.utils.CookieUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理后台JWT令牌校验的拦截器 - 支持Cookie-only模式，与用户端完全隔离
 */
@Component
@Slf4j
public class JwtTokenAdminInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 校验JWT
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            // 当前拦截到的不是动态方法，直接放行
            return true;
        }

        String requestURI = request.getRequestURI();
        log.debug("🔍 管理后台JWT拦截器处理请求: {}", requestURI);

        // 1. 从Cookie或Header中获取管理员token
        String token = null;

        // 优先从管理后台专用Cookie获取token
        token = getAdminTokenFromCookie(request);
        if (token != null) {
            log.debug("✅ 从管理后台Cookie获取到token");
        } else {
            // 兜底：从请求头获取token（向后兼容）
            token = request.getHeader("token");
            if (token != null) {
                log.debug("✅ 从请求头获取到管理员token");
            }
        }

        // 2. 校验令牌
        if (token == null || token.isEmpty()) {
            log.warn("❌ 管理后台请求缺少Access Token，尝试使用Refresh Token: {}", requestURI);
            
            // 尝试使用Refresh Token自动刷新
            String refreshedToken = tryRefreshAdminToken(request, response);
            if (refreshedToken == null) {
                log.error("❌ 管理员认证失败且无法刷新Token: {}", requestURI);
                response.setStatus(401);
                return false;
            }
            
            // 刷新成功，直接使用返回的新token验证
            try {
                Claims newClaims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), refreshedToken);
                
                // 设置管理员信息到BaseContext
                Long empId = Long.valueOf(newClaims.get(JwtClaimsConstant.EMP_ID).toString());
                String username = newClaims.get(JwtClaimsConstant.USERNAME) != null ? 
                                 newClaims.get(JwtClaimsConstant.USERNAME).toString() : null;
                String userType = newClaims.get(JwtClaimsConstant.USER_TYPE) != null ?
                                 newClaims.get(JwtClaimsConstant.USER_TYPE).toString() : "admin";
                
                BaseContext.setCurrentId(empId);
                BaseContext.setCurrentUsername(username);
                BaseContext.setCurrentUserType(userType);
                BaseContext.setCurrentAgentId(null);
                BaseContext.setCurrentOperatorId(null);
                
                log.info("✅ 管理员Token刷新后验证成功: empId={}, username={}", empId, username);
                return true;
                
            } catch (Exception refreshEx) {
                log.error("❌ 刷新后的Token验证失败: {}", refreshEx.getMessage());
                response.setStatus(401);
                return false;
            }
        }

        try {
            log.info("🔍 管理员JWT验证开始: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            
            // 检查Token是否即将过期（提前30分钟刷新）
            long currentTime = System.currentTimeMillis() / 1000;
            long exp = claims.getExpiration().getTime() / 1000;
            long timeUntilExpiry = exp - currentTime;
            
            log.debug("🕐 Token剩余时间: {}秒", timeUntilExpiry);
            
            // 如果Token在5分钟内过期，尝试自动刷新
            if (timeUntilExpiry < 5 * 60) { // 5分钟 = 300秒
                log.info("⏰ 管理员Token即将过期，开始自动刷新");
                String newToken = tryRefreshAdminToken(request, response);
                if (newToken == null) {
                    log.warn("❌ Token自动刷新失败，但继续使用当前Token");
                    // 不阻止请求，让用户继续使用当前token直到真正过期
                } else {
                    log.info("✅ Token自动刷新成功，已设置新Token到Cookie");
                }
            }
            
            // 3. 提取管理员信息并设置到BaseContext
            Long empId = Long.valueOf(claims.get(JwtClaimsConstant.EMP_ID).toString());
            String username = claims.get(JwtClaimsConstant.USERNAME) != null ? 
                             claims.get(JwtClaimsConstant.USERNAME).toString() : null;
            String userType = claims.get(JwtClaimsConstant.USER_TYPE) != null ?
                             claims.get(JwtClaimsConstant.USER_TYPE).toString() : "admin";

            log.info("🎯 管理员身份验证成功: empId={}, username={}, userType={}", empId, username, userType);

            // 设置管理员信息到BaseContext
            BaseContext.setCurrentId(empId);
            BaseContext.setCurrentUsername(username);
            BaseContext.setCurrentUserType(userType);
            
            // 管理员不需要设置agentId和operatorId
            BaseContext.setCurrentAgentId(null);
            BaseContext.setCurrentOperatorId(null);

            log.debug("✅ 管理员BaseContext设置完成: empId={}, userType={}", empId, userType);

        } catch (Exception ex) {
            log.warn("❌ 管理员JWT验证失败，尝试使用Refresh Token: {}", ex.getMessage());
            
            // JWT验证失败，尝试使用Refresh Token自动刷新
            String newToken = tryRefreshAdminToken(request, response);
            if (newToken == null) {
                log.error("❌ 管理员认证失败且无法刷新Token");
                response.setStatus(401);
                return false;
            }
            
            // 刷新成功，直接使用返回的新token验证
            try {
                Claims newClaims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), newToken);
                
                // 设置管理员信息到BaseContext
                Long empId = Long.valueOf(newClaims.get(JwtClaimsConstant.EMP_ID).toString());
                String username = newClaims.get(JwtClaimsConstant.USERNAME) != null ? 
                                 newClaims.get(JwtClaimsConstant.USERNAME).toString() : null;
                String userType = newClaims.get(JwtClaimsConstant.USER_TYPE) != null ?
                                 newClaims.get(JwtClaimsConstant.USER_TYPE).toString() : "admin";
                
                BaseContext.setCurrentId(empId);
                BaseContext.setCurrentUsername(username);
                BaseContext.setCurrentUserType(userType);
                BaseContext.setCurrentAgentId(null);
                BaseContext.setCurrentOperatorId(null);
                
                log.info("✅ 管理员Token刷新后验证成功: empId={}, username={}", empId, username);
                
            } catch (Exception refreshEx) {
                log.error("❌ 刷新后的Token验证失败: {}", refreshEx.getMessage());
                response.setStatus(401);
                return false;
            }
        }

        // 3. 通过，放行
        log.debug("✅ 管理后台请求认证通过: {}", requestURI);
        return true;
    }

    /**
     * 从管理后台专用Cookie中获取token
     */
    private String getAdminTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            // 记录所有Cookie用于调试
            StringBuilder cookieDebug = new StringBuilder("管理后台Cookie检查: ");
            for (Cookie cookie : request.getCookies()) {
                String cookieValue = cookie.getValue();
                String displayValue = cookieValue != null && cookieValue.length() > 10 ? 
                                    cookieValue.substring(0, 10) + "..." : cookieValue;
                cookieDebug.append(cookie.getName()).append("=").append(displayValue).append(", ");
            }
            log.info(cookieDebug.toString()); // 改为info级别，便于调试

            // 管理后台专用Cookie名称，避免与用户端冲突
            String[] adminTokenCookieNames = {"adminToken", "adminAuthToken", "admin_token"};

            for (String cookieName : adminTokenCookieNames) {
                for (Cookie cookie : request.getCookies()) {
                    if (cookieName.equals(cookie.getName())) {
                        String tokenValue = cookie.getValue();
                        if (tokenValue != null && !tokenValue.trim().isEmpty() && 
                            !"null".equals(tokenValue) && !"undefined".equals(tokenValue)) {
                            log.info("✅ 从管理后台Cookie中找到有效token: {}", cookieName);
                            return tokenValue;
                        } else {
                            log.warn("❌ 找到Cookie {} 但值无效: '{}'", cookieName, tokenValue);
                        }
                    }
                }
            }
        } else {
            log.warn("❌ 请求中没有任何Cookie");
        }

        log.warn("🔍 未从管理后台Cookie中找到有效token");
        return null;
    }

    /**
     * 尝试刷新管理员Token，返回新的Access Token
     */
    private String tryRefreshAdminToken(HttpServletRequest request, HttpServletResponse response) {
        try {
            log.info("🔄 开始管理员Token刷新");
            
            // 从Cookie中获取refreshToken
            String refreshToken = getAdminRefreshTokenFromCookie(request);
            if (refreshToken == null || refreshToken.isEmpty()) {
                log.warn("❌ AdminRefreshToken为空，无法刷新");
                return null;
            }
            
            // 验证refreshToken
            Claims refreshClaims;
            try {
                refreshClaims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), refreshToken);
                log.debug("✅ AdminRefreshToken验证成功");
            } catch (Exception e) {
                log.warn("❌ AdminRefreshToken无效或已过期: {}", e.getMessage());
                // 清除无效的refresh token cookie
                CookieUtil.clearCookie(response, "adminRefreshToken", "/");
                CookieUtil.clearCookie(response, "adminRefreshToken", "/admin");
                return null;
            }
            
            // 从refreshToken中提取管理员信息
            Long empId = Long.valueOf(refreshClaims.get(JwtClaimsConstant.EMP_ID).toString());
            String username = refreshClaims.get(JwtClaimsConstant.USERNAME) != null ? 
                             refreshClaims.get(JwtClaimsConstant.USERNAME).toString() : null;
            String userType = refreshClaims.get(JwtClaimsConstant.USER_TYPE) != null ?
                             refreshClaims.get(JwtClaimsConstant.USER_TYPE).toString() : "admin";
            
            if (empId == null || username == null) {
                log.warn("❌ 无法从AdminRefreshToken中提取管理员信息");
                return null;
            }
            
            log.info("🔍 从RefreshToken提取信息: empId={}, username={}, userType={}", empId, username, userType);
            
            // 生成新的access token
            Map<String, Object> claims = new HashMap<>();
            claims.put(JwtClaimsConstant.EMP_ID, empId);
            claims.put(JwtClaimsConstant.USERNAME, username);
            claims.put(JwtClaimsConstant.USER_TYPE, userType);
            claims.put("username", username);
            claims.put("userType", userType);
            
            String newAccessToken = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(), // 15分钟
                claims
            );
            
            // 设置新的access token cookie（15分钟）
            CookieUtil.setAdminCookieWithMultiplePaths(response, "adminToken", newAccessToken, true, 15 * 60);
            
            // 检查refreshToken是否需要更新（如果剩余时间少于1小时）
            long currentTime = System.currentTimeMillis() / 1000;
            long refreshExp = refreshClaims.getExpiration().getTime() / 1000;
            long refreshTimeUntilExpiry = refreshExp - currentTime;
            
            if (refreshTimeUntilExpiry < 1 * 60 * 60) { // 如果refreshToken在1小时内过期
                log.info("🔄 RefreshToken即将过期，生成新的RefreshToken");
                
                Map<String, Object> newRefreshClaims = new HashMap<>();
                newRefreshClaims.put(JwtClaimsConstant.EMP_ID, empId);
                newRefreshClaims.put(JwtClaimsConstant.USERNAME, username);
                newRefreshClaims.put(JwtClaimsConstant.USER_TYPE, userType);
                
                String newRefreshToken = JwtUtil.createJWT(
                    jwtProperties.getAdminSecretKey(),
                    7 * 24 * 60 * 60 * 1000L, // 7天
                    newRefreshClaims
                );
                
                CookieUtil.setAdminCookieWithMultiplePaths(response, "adminRefreshToken", newRefreshToken, true, 7 * 24 * 60 * 60);
                log.info("✅ 已更新AdminRefreshToken");
            }
            
            log.info("✅ 管理员Token刷新成功: empId={}, username={}", empId, username);
            return newAccessToken;
            
        } catch (Exception e) {
            log.error("❌ 管理员Token刷新失败", e);
            return null;
        }
    }
    
    /**
     * 从管理后台专用Cookie中获取refresh token
     */
    private String getAdminRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            log.info("🔍 查找adminRefreshToken，当前Cookie数量: {}", request.getCookies().length);
            
            // 管理后台专用RefreshToken Cookie名称
            String[] adminRefreshTokenCookieNames = {"adminRefreshToken", "admin_refresh_token"};

            for (String cookieName : adminRefreshTokenCookieNames) {
                for (Cookie cookie : request.getCookies()) {
                    if (cookieName.equals(cookie.getName())) {
                        String tokenValue = cookie.getValue();
                        if (tokenValue != null && !tokenValue.trim().isEmpty() && 
                            !"null".equals(tokenValue) && !"undefined".equals(tokenValue)) {
                            log.info("✅ 从管理后台Cookie中找到RefreshToken: {}", cookieName);
                            return tokenValue;
                        } else {
                            log.warn("❌ 找到RefreshToken Cookie {} 但值无效: '{}'", cookieName, tokenValue);
                        }
                    }
                }
            }
        } else {
            log.warn("❌ 查找RefreshToken时请求中没有任何Cookie");
        }

        log.warn("🔍 未从管理后台Cookie中找到RefreshToken");
        return null;
    }

    /**
     * 目标资源方法执行完成后
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清理BaseContext，避免内存泄漏
        BaseContext.removeAll();
        log.debug("🧹 管理后台请求完成，已清理BaseContext");
    }
}
