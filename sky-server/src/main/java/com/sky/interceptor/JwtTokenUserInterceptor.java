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
 * jwt令牌校验的拦截器
 */
@Component
@Slf4j
public class JwtTokenUserInterceptor implements HandlerInterceptor {

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
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("用户JWT拦截器...");
        
        //判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            //当前拦截到的不是动态方法，直接放行
            return true;
        }
        
        // 获取请求路径
        String requestURI = request.getRequestURI();
        log.debug("拦截请求: {}", requestURI);
        
        // 检查是否是不需要验证的公共API路径
        if(requestURI.contains("/user/login") || 
           requestURI.contains("/user/register") || 
           requestURI.contains("/user/shop/status") ||
           requestURI.contains("/user/day-tours") ||
           requestURI.contains("/user/group-tours") ||
           requestURI.contains("/user/tours/") ||
           requestURI.contains("/user/tours/hot") ||
           requestURI.contains("/user/tours/recommended") ||
           requestURI.contains("/user/tours/search") ||
           requestURI.contains("/agent/login") ||
           requestURI.contains("/user/agent/login")) {
            log.debug("公共API无需验证: {}", requestURI);
            return true;
        }
        
        // 特殊判断旅游详情API
        if (requestURI.matches("/user/tours/\\d+")) {
            log.info("旅游详情API，无需验证: {}", requestURI);
            return true;
        }

        //1、尝试从请求头中获取令牌（多种方式）
        String token = null;
        
        // 首先尝试从配置的用户token名称中获取
        token = request.getHeader(jwtProperties.getUserTokenName());
        
        // 如果上面方式获取失败，尝试从"token"头获取
        if (token == null) {
            token = request.getHeader("token");
        }
        
        // 如果上面方式获取失败，尝试从"Authentication"头获取
        if (token == null) {
            token = request.getHeader("Authentication");
        }
        
        // 如果上面方式获取失败，尝试从"Authorization"头获取（可能带有Bearer前缀）
        if (token == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }
        
        // 只在开发环境或调试模式下输出token信息
        if (log.isDebugEnabled()) {
            log.debug("用户jwt校验:{}", token);
        }

        if (token == null) {
            //未携带token，不通过，响应401状态码
            log.debug("用户jwt校验:token为空");
            response.setStatus(401);
            return false;
        }

        //2、校验令牌
        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
            log.info("用户ID:{}", userId);
            BaseContext.setCurrentId(userId);
            
            // 从JWT中获取用户名并存入BaseContext
            String username = claims.get(JwtClaimsConstant.USERNAME, String.class);
            if (username != null) {
                BaseContext.setCurrentUsername(username);
                log.info("当前用户: {}, ID: {}", username, userId);
            }
            
            // 从JWT中获取用户类型并存入BaseContext
            String userType = claims.get(JwtClaimsConstant.USER_TYPE, String.class);
            if (userType != null) {
                BaseContext.setCurrentUserType(userType);
                log.info("用户类型: {}", userType);
            } else {
                // 默认设置为普通用户
                BaseContext.setCurrentUserType("regular");
            }
            
            // 从JWT中获取代理商ID并存入BaseContext（如果存在）
            if (claims.get(JwtClaimsConstant.AGENT_ID) != null) {
                Long agentId = Long.valueOf(claims.get(JwtClaimsConstant.AGENT_ID).toString());
                BaseContext.setCurrentAgentId(agentId);
                log.info("代理商ID: {}", agentId);
            }
            
            // 检查请求路径是否限制仅代理商访问
            if ((requestURI.startsWith("/agent/") && !requestURI.equals("/agent/login")) 
                    && !"agent".equals(userType)) {
                log.warn("非代理商用户访问代理商接口: {}", requestURI);
                response.setStatus(403); // 权限不足
                return false;
            }
            
            //3、通过，放行
            return true;
        } catch (Exception ex) {
            //降低日志级别，避免大量错误日志
            log.debug("用户jwt校验异常: {}", ex.getMessage());
            //4、不通过，响应401状态码
            response.setStatus(401);
            return false;
        }
    }
}
