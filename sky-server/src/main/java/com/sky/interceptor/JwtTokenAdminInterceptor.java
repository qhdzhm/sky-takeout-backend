package com.sky.interceptor;

import com.sky.constant.JwtClaimsConstant;
import com.sky.context.BaseContext;
import com.sky.properties.JwtProperties;
import com.sky.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
            log.warn("❌ 管理后台请求缺少认证信息: {}", requestURI);
            response.setStatus(401);
            return false;
        }

        try {
            log.info("🔍 管理员JWT验证开始: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            
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
            log.error("❌ 管理员JWT验证失败: {}", ex.getMessage());
            response.setStatus(401);
            return false;
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
            log.debug(cookieDebug.toString());

            // 管理后台专用Cookie名称，避免与用户端冲突
            String[] adminTokenCookieNames = {"adminToken", "adminAuthToken", "admin_token"};

            for (String cookieName : adminTokenCookieNames) {
                for (Cookie cookie : request.getCookies()) {
                    if (cookieName.equals(cookie.getName())) {
                        String tokenValue = cookie.getValue();
                        if (tokenValue != null && !tokenValue.trim().isEmpty() && 
                            !"null".equals(tokenValue) && !"undefined".equals(tokenValue)) {
                            log.debug("✅ 从管理后台Cookie中找到有效token: {}", cookieName);
                            return tokenValue;
                        }
                    }
                }
            }
        }

        log.debug("🔍 未从管理后台Cookie中找到有效token");
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
