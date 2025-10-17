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
        
        // 🔧 新增：检查是否已经被代理商拦截器验证过
        Long existingUserId = BaseContext.getCurrentId();
        String existingUserType = BaseContext.getCurrentUserType();
        if (existingUserId != null && "agent".equals(existingUserType)) {
            log.info("⚡ 代理商已通过认证，用户拦截器跳过验证: userId={}, path={}", existingUserId, requestURI);
            return true; // 代理商已验证，跳过用户验证
        }
        
        // 检查是否是不需要验证的公共API路径
        if(requestURI.contains("/user/login") || 
           requestURI.contains("/user/register") || 
           requestURI.contains("/user/shop/status") ||
           requestURI.contains("/user/wechat/qrcode-url") ||
           requestURI.contains("/user/day-tours") ||
           requestURI.contains("/user/group-tours") ||
           requestURI.contains("/user/tours/") ||
           requestURI.contains("/user/tours/hot") ||
           requestURI.contains("/user/tours/recommended") ||
           requestURI.contains("/user/tours/search") ||
           requestURI.contains("/agent/login")) {
            log.debug("公共API无需验证: {}", requestURI);
            return true;
        }
        
        // 特殊判断旅游详情API
        if (requestURI.matches("/user/tours/\\d+")) {
            log.info("旅游详情API，无需验证: {}", requestURI);
            return true;
        }

        //1、尝试获取令牌（优先从HttpOnly Cookie，然后从请求头）
        String token = null;
        log.info("🔍 开始获取JWT token，请求路径: {}", requestURI);
        
        // 首先尝试从HttpOnly Cookie获取token
        token = getTokenFromCookie(request);
        if (token != null) {
            log.info("✅ 从HttpOnly Cookie获取到token，长度: {}", token.length());
        } else {
            log.info("❌ HttpOnly Cookie中没有找到token");
        }
        
        // 如果Cookie中没有token，尝试从请求头获取（向后兼容）
        if (token == null) {
            log.info("🔍 尝试从请求头获取token...");
            
            // 首先尝试从配置的用户token名称中获取
            token = request.getHeader(jwtProperties.getUserTokenName());
            if (token != null) {
                log.info("✅ 从配置的用户token名称({})获取到token", jwtProperties.getUserTokenName());
            }
            
            // 如果上面方式获取失败，尝试从"token"头获取
            if (token == null) {
                token = request.getHeader("token");
                if (token != null) {
                    log.info("✅ 从'token'请求头获取到token");
                }
            }
            
            // 如果上面方式获取失败，尝试从"Authentication"头获取
            if (token == null) {
                token = request.getHeader("Authentication");
                if (token != null) {
                    log.info("✅ 从'Authentication'请求头获取到token");
                }
            }
            
            // 如果上面方式获取失败，尝试从"Authorization"头获取（可能带有Bearer前缀）
            if (token == null) {
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7);
                    log.info("✅ 从'Authorization'请求头获取到Bearer token");
                } else if (authHeader != null) {
                    log.info("🔍 'Authorization'请求头存在但不是Bearer格式: {}", authHeader.substring(0, Math.min(20, authHeader.length())) + "...");
                }
            }
            
            if (token == null) {
                log.warn("❌ 所有请求头中都没有找到token");
            }
        }
        
        // 只在开发环境或调试模式下输出token信息
        if (log.isDebugEnabled()) {
            log.debug("用户jwt校验:{}", token);
        }

        if (token == null) {
            // 仅允许未登录访问“计算价格”接口；创建订单必须登录
            if (requestURI.contains("/user/bookings/tour/calculate-price")) {
                log.info("✅ 游客模式访问计算价格接口: {}", requestURI);
                // 游客模式，清空BaseContext确保没有用户信息
                BaseContext.setCurrentId(null);
                BaseContext.setCurrentUserType(null);
                BaseContext.setCurrentAgentId(null);
                BaseContext.setCurrentOperatorId(null);
                BaseContext.setCurrentUsername(null);
                return true;
            }

            //未携带token，不通过，响应401状态码
            log.debug("用户jwt校验:token为空");
            response.setStatus(401);
            return false;
        }

        //2、校验令牌
        try {
            log.info("🔍 开始解析JWT token，token长度: {}", token.length());
            Claims claims = null;
            try {
                // 首先尝试用户密钥解析
                log.debug("尝试使用用户密钥解析JWT...");
                claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
                log.info("✅ 使用用户密钥解析JWT成功");
            } catch (Exception e) {
                log.warn("❌ 用户密钥解析失败: {}", e.getMessage());
                // 如果用户密钥解析失败，尝试代理商密钥
                try {
                    log.debug("尝试使用代理商密钥解析JWT...");
                    claims = JwtUtil.parseJWT(jwtProperties.getAgentSecretKey(), token);
                    log.info("✅ 使用代理商密钥解析JWT成功");
                } catch (Exception ex) {
                    log.error("❌ 代理商密钥解析也失败: {}", ex.getMessage());
                    log.debug("JWT解析失败: {}", ex.getMessage());
                    response.setStatus(401);
                    return false;
                }
            }
            
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
            log.info("🔍 从JWT解析用户ID: {}", userId);
            BaseContext.setCurrentId(userId);
            log.info("✅ BaseContext.setCurrentId({}) 设置完成", userId);
            
            // 从JWT中获取用户名并存入BaseContext
            String username = claims.get(JwtClaimsConstant.USERNAME, String.class);
            if (username != null) {
                BaseContext.setCurrentUsername(username);
                log.info("✅ 设置用户名到BaseContext: {}, ID: {}", username, userId);
            } else {
                log.warn("⚠️ JWT中没有用户名信息");
            }
            
            // 从JWT中获取用户类型并存入BaseContext
            String userType = claims.get(JwtClaimsConstant.USER_TYPE, String.class);
            if (userType != null) {
                BaseContext.setCurrentUserType(userType);
                log.info("✅ 设置用户类型到BaseContext: {}", userType);
            } else {
                // 默认设置为普通用户
                BaseContext.setCurrentUserType("regular");
                log.warn("⚠️ JWT中没有用户类型信息，默认设置为regular");
            }
            
            // 从JWT中获取代理商ID并存入BaseContext（只有代理商用户才设置）
            Object agentIdClaim = claims.get(JwtClaimsConstant.AGENT_ID);
            log.info("🔍 JWT中的代理商ID声明: {}, 用户类型: {}", agentIdClaim, userType);
            
            if (agentIdClaim != null && 
                ("agent".equals(userType) || "agent_operator".equals(userType))) {
                Long agentId = Long.valueOf(agentIdClaim.toString());
                BaseContext.setCurrentAgentId(agentId);
                log.info("✅ 设置代理商ID到BaseContext: {}", agentId);
            } else {
                // 清空代理商ID，确保普通用户不会获得代理商折扣
                BaseContext.setCurrentAgentId(null);
                if (agentIdClaim != null) {
                    log.warn("⚠️ 普通用户({})的Token中包含代理商ID({}), 已忽略", userType, agentIdClaim);
                } else {
                    log.info("🔍 JWT中没有代理商ID声明，设置为null");
                }
            }
            
            // 从JWT中获取操作员ID并存入BaseContext（如果存在）
            Object operatorIdClaim = claims.get(JwtClaimsConstant.OPERATOR_ID);
            if (operatorIdClaim != null) {
                Long operatorId = Long.valueOf(operatorIdClaim.toString());
                BaseContext.setCurrentOperatorId(operatorId);
                log.info("✅ 设置操作员ID到BaseContext: {}", operatorId);
            } else {
                log.info("🔍 JWT中没有操作员ID声明");
            }
            
            // 允许代理商访问tour-bookings相关接口
            if (requestURI.contains("/user/tour-bookings") && "agent".equals(userType)) {
                log.info("允许代理商访问旅游订单接口: {}", requestURI);
                return true;
            }
            
            // 检查请求路径是否限制仅代理商访问
            if ((requestURI.startsWith("/agent/") && !requestURI.equals("/agent/login")) 
                    && !"agent".equals(userType) && !"agent_operator".equals(userType)) {
                log.warn("非代理商用户访问代理商接口: {}, 用户类型: {}", requestURI, userType);
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
    
    /**
     * 从HttpOnly Cookie中获取token
     */
    private String getTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            // 🔍 详细记录所有Cookie
            StringBuilder cookieList = new StringBuilder();
            String authToken = null;
            String refreshToken = null;
            
            for (Cookie cookie : request.getCookies()) {
                cookieList.append(cookie.getName()).append(", ");
                
                // 优先查找 authToken
                if ("authToken".equals(cookie.getName()) || 
                    "token".equals(cookie.getName()) ||
                    "userToken".equals(cookie.getName()) ||
                    "jwt".equals(cookie.getName())) {
                    authToken = cookie.getValue();
                    log.info("✅ 从Cookie中找到认证token: {}", cookie.getName());
                }
                
                // 也记录 refreshToken（作为备用）
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    log.info("🔄 从Cookie中找到refreshToken");
                }
            }
            
            // 如果有 authToken，优先使用
            if (authToken != null) {
                return authToken;
            }
            
            // 如果没有 authToken 但有 refreshToken，使用 refreshToken
            if (refreshToken != null) {
                log.info("⚡ authToken已过期，使用refreshToken进行认证");
                return refreshToken;
            }
            
            log.warn("❌ 未找到认证token Cookie，所有Cookie: [{}]", cookieList.toString());
        } else {
            log.warn("❌ 请求中没有任何Cookie");
        }
        return null;
    }
    
    /**
     * 目标资源方法执行完成后：清理ThreadLocal，避免上下文串号
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        BaseContext.removeAll();
        log.debug("🧹 用户端请求完成，已清理BaseContext");
    }
} 

