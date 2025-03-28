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
public class JwtTokenAdminInterceptor implements HandlerInterceptor {

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
        //判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            //当前拦截到的不是动态方法，直接放行
            return true;
        }

        //1、尝试从请求头中获取令牌（多种方式）
        String token = null;
        
        // 首先尝试从配置的adminToken名称中获取
        token = request.getHeader(jwtProperties.getAdminTokenName());
        
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
            log.debug("管理员jwt校验:{}", token);
        }

        if (token == null) {
            //未携带token，不通过，响应401状态码
            log.debug("管理员jwt校验:token为空");
            response.setStatus(401);
            return false;
        }

        //2、校验令牌
        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            Long empId = Long.valueOf(claims.get(JwtClaimsConstant.EMP_ID).toString());
            log.info("管理员ID:{}", empId);
            BaseContext.setCurrentId(empId);
            
            // 从JWT中获取用户名并存入BaseContext
            String username = claims.get("username", String.class);
            if (username != null) {
                BaseContext.setCurrentUsername(username);
                log.info("当前管理员: {}, ID: {}", username, empId);
            }
            
            //3、通过，放行
            return true;
        } catch (Exception ex) {
            //降低日志级别，避免大量错误日志
            log.debug("管理员jwt校验异常: {}", ex.getMessage());
            //4、不通过，响应401状态码
            response.setStatus(401);
            return false;
        }
    }
}
