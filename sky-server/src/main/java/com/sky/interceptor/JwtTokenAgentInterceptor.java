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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * jwt令牌校验的拦截器（代理商端）
 */
@Component
@Slf4j
public class JwtTokenAgentInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

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

        // 1. 从请求头中获取令牌
        String token = request.getHeader(jwtProperties.getAgentTokenName());
        
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
        
        // 2. 校验令牌
        try {
            log.info("代理商JWT校验:{}", token);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAgentSecretKey(), token);
            
            // 3. 获取用户ID、用户名和用户类型
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
            String username = claims.get("username") != null ? claims.get("username").toString() : null;
            String userType = claims.get("userType") != null ? claims.get("userType").toString() : "agent";
            
            // 4. 检查用户类型
            if (!"agent".equals(userType)) {
                log.warn("非代理商用户尝试访问代理商接口, 用户类型:{}", userType);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
            
            // 5. 获取代理商ID
            Long agentId = null;
            if (claims.get(JwtClaimsConstant.AGENT_ID) != null) {
                agentId = Long.valueOf(claims.get(JwtClaimsConstant.AGENT_ID).toString());
            } else {
                // 如果没有专门的代理商ID字段，则代理商ID就是用户ID
                agentId = userId;
            }
            
            // 6. 将用户信息存储到ThreadLocal
            BaseContext.setCurrentId(userId);
            if (username != null) {
                BaseContext.setCurrentUsername(username);
            }
            BaseContext.setCurrentUserType(userType);
            BaseContext.setCurrentAgentId(agentId);
            
            log.info("JWT校验通过，用户ID:{}, 用户名:{}, 用户类型:{}, 代理商ID:{}", userId, username, userType, agentId);
            
            return true;
        } catch (Exception ex) {
            log.error("JWT令牌校验失败", ex);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求完成后，清除当前线程的用户信息
        BaseContext.removeAll();
    }
} 