package com.sky.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.constant.JwtClaimsConstant;
import com.sky.context.BaseContext;
import com.sky.properties.JwtProperties;
import com.sky.utils.JwtUtil;
import com.sky.result.Result;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * jwt令牌校验的拦截器（代理商端）
 */
@Component
@Slf4j
public class JwtTokenAgentInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;
    
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 校验jwt
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            // 当前拦截到的不是Controller的方法，直接放行
            return true;
        }

        // 1. 尝试获取令牌（优先从HttpOnly Cookie，然后从请求头）
        String token = null;
        
        // 首先尝试从HttpOnly Cookie获取token
        token = getTokenFromCookie(request);
        if (token != null) {
            log.debug("从HttpOnly Cookie获取到token");
        } else {
            // 如果Cookie中没有token，尝试从请求头获取（向后兼容）
            token = request.getHeader(jwtProperties.getAgentTokenName());
            
            // 尝试从其他可能的地方获取令牌
            if (token == null) {
                token = request.getHeader("Authorization");
                if (token != null && token.startsWith("Bearer ")) {
                    token = token.substring(7);
                }
            }
            
            if (token == null) {
                token = request.getHeader("Authentication");
            }
            
            if (token == null) {
                token = request.getHeader("token");
            }
            
            if (token != null) {
                log.debug("从请求头获取到token");
            }
        }
        
        // 2. 校验令牌
        try {
            log.info("代理商JWT校验:{}", token);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAgentSecretKey(), token);
            
            // 3. 获取用户ID、用户名和用户类型
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
            String username = claims.get("username") != null ? claims.get("username").toString() : null;
            String userType = claims.get("userType") != null ? claims.get("userType").toString() : "agent";
            
            // 4. 检查用户类型
            if (!"agent".equals(userType) && !"agent_operator".equals(userType)) {
                log.warn("非代理商用户尝试访问代理商接口, 用户类型:{}", userType);
                handleError(response, "非代理商用户，无权访问此接口", 403);
                return false;
            }
            
            // 5. 获取代理商ID
            Long agentId = null;
            if (claims.get(JwtClaimsConstant.AGENT_ID) != null) {
                agentId = Long.valueOf(claims.get(JwtClaimsConstant.AGENT_ID).toString());
            } else if ("agent".equals(userType)) {
                // 如果是代理商主账号且没有专门的代理商ID字段，则代理商ID就是用户ID
                agentId = userId;
            }
            
            // 6. 获取操作员ID（如果存在）
            Long operatorId = null;
            if (claims.get(JwtClaimsConstant.OPERATOR_ID) != null) {
                operatorId = Long.valueOf(claims.get(JwtClaimsConstant.OPERATOR_ID).toString());
            }
            
            // 7. 将用户信息存储到ThreadLocal
            BaseContext.setCurrentId(userId);
            if (username != null) {
                BaseContext.setCurrentUsername(username);
            }
            BaseContext.setCurrentUserType(userType);
            if (agentId != null) {
                BaseContext.setCurrentAgentId(agentId);
            }
            if (operatorId != null) {
                BaseContext.setCurrentOperatorId(operatorId);
            }
            
            // 🔍 详细记录BaseContext设置状态
            log.info("🔍 JWT校验通过，BaseContext设置完成:");
            log.info("  - 用户ID: {} (设置状态: {})", userId, BaseContext.getCurrentId());
            log.info("  - 用户名: {} (设置状态: {})", username, BaseContext.getCurrentUsername());
            log.info("  - 用户类型: {} (设置状态: {})", userType, BaseContext.getCurrentUserType());
            log.info("  - 代理商ID: {} (设置状态: {})", agentId, BaseContext.getCurrentAgentId());
            log.info("  - 操作员ID: {} (设置状态: {})", operatorId, BaseContext.getCurrentOperatorId());
            
            // 🔒 验证BaseContext设置是否成功
            if (BaseContext.getCurrentId() == null || !BaseContext.getCurrentId().equals(userId)) {
                log.error("❌ BaseContext.setCurrentId设置失败！预期: {}, 实际: {}", userId, BaseContext.getCurrentId());
            }
            if (agentId != null && (BaseContext.getCurrentAgentId() == null || !BaseContext.getCurrentAgentId().equals(agentId))) {
                log.error("❌ BaseContext.setCurrentAgentId设置失败！预期: {}, 实际: {}", agentId, BaseContext.getCurrentAgentId());
            }
            
            return true;
        } catch (ExpiredJwtException ex) {
            log.warn("JWT令牌已过期: {}", ex.getMessage());
            handleError(response, "登录已过期，请重新登录", 401);
            return false;
        } catch (SignatureException ex) {
            log.warn("JWT签名验证失败: {}", ex.getMessage());
            handleError(response, "无效的令牌签名", 401);
            return false;
        } catch (Exception ex) {
            log.error("JWT令牌校验失败: {}", ex.getMessage());
            handleError(response, "令牌校验失败，请重新登录", 401);
            return false;
        }
    }
    
    /**
     * 处理错误响应
     * 
     * @param response HTTP响应对象
     * @param message 错误消息
     * @param statusCode HTTP状态码
     * @throws IOException 
     */
    private void handleError(HttpServletResponse response, String message, int statusCode) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json;charset=UTF-8");
        
        Result<String> result = Result.error(message);
        result.setCode(statusCode);
        
        String jsonResponse = objectMapper.writeValueAsString(result);
        
        try (PrintWriter writer = response.getWriter()) {
            writer.write(jsonResponse);
            writer.flush();
        }
    }
    
    /**
     * 从HttpOnly Cookie中获取token - 增强跨平台兼容性
     */
    private String getTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            // 记录所有Cookie用于调试
            StringBuilder cookieDebug = new StringBuilder("所有Cookie: ");
            for (Cookie cookie : request.getCookies()) {
                cookieDebug.append(cookie.getName()).append("=").append(cookie.getValue().substring(0, Math.min(cookie.getValue().length(), 10))).append("..., ");
            }
            log.debug(cookieDebug.toString());
            
            // 按优先级查找token
            String[] tokenCookieNames = {"authToken", "token", "refreshToken", "agentToken"};
            
            for (String cookieName : tokenCookieNames) {
                for (Cookie cookie : request.getCookies()) {
                    if (cookieName.equals(cookie.getName())) {
                        String tokenValue = cookie.getValue();
                        if (tokenValue != null && !tokenValue.trim().isEmpty() && !"null".equals(tokenValue)) {
                            log.debug("从Cookie中找到有效token: {}", cookieName);
                            return tokenValue;
                        }
                    }
                }
            }
        }
        log.debug("未从Cookie中找到有效token");
        return null;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 🔍 记录请求完成前的BaseContext状态
        String requestUri = request.getRequestURI();
        log.debug("🔍 请求完成，准备清理BaseContext: {} - 当前状态: userId={}, agentId={}, userType={}", 
                 requestUri, BaseContext.getCurrentId(), BaseContext.getCurrentAgentId(), BaseContext.getCurrentUserType());
        
        // 请求完成后，清除当前线程的用户信息
        BaseContext.removeAll();
        
        log.debug("✅ BaseContext已清理完成: {}", requestUri);
    }
} 