package com.sky.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 安全配置类 - 支持HttpOnly Cookies和CSRF保护
 */
@Configuration
@Slf4j
public class SecurityConfig {

    // CSRF Token存储（生产环境应使用Redis）
    private static final ConcurrentHashMap<String, String> csrfTokenStore = new ConcurrentHashMap<>();
    
    /**
     * CORS配置源 - 临时禁用，使用WebMvcConfiguration中的配置
     */
    // @Bean
    public CorsConfigurationSource corsConfigurationSource_disabled() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 允许的域名（生产环境应该指定具体域名）
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:3000",  // 用户端前端
            "http://localhost:3001",  // 管理后台前端
            "http://127.0.0.1:3000",  // 用户端前端
            "http://127.0.0.1:3001",  // 管理后台前端
            "https://htas.com.au",    // 生产环境前端
            "http://htas.com.au",     // 生产环境前端(HTTP)
            "https://www.htas.com.au", // 生产环境前端(带www)
            "http://www.htas.com.au"   // 生产环境前端(带www,HTTP)
        ));
        
        // 允许的HTTP方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // 允许的请求头
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // 允许发送Cookie
        configuration.setAllowCredentials(true);
        
        // 预检请求缓存时间
        configuration.setMaxAge(3600L);
        
        // 暴露的响应头
        configuration.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Methods", 
            "Access-Control-Allow-Headers",
            "Access-Control-Allow-Credentials",
            "X-CSRF-Token"
        ));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    /**
     * 安全头部过滤器
     */
    @Bean
    public FilterRegistrationBean<SecurityHeadersFilter> securityHeadersFilter() {
        FilterRegistrationBean<SecurityHeadersFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new SecurityHeadersFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }
    
    /**
     * CSRF保护过滤器 - 暂时禁用，因为这是API服务，已有JWT和CORS保护
     */
    // @Bean
    public FilterRegistrationBean<CsrfProtectionFilter> csrfProtectionFilter_disabled() {
        FilterRegistrationBean<CsrfProtectionFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new CsrfProtectionFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(2);
        return registrationBean;
    }
    
    /**
     * 安全头部过滤器实现
     */
    public static class SecurityHeadersFilter implements Filter {
        
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            
            // 内容安全策略
            httpResponse.setHeader("Content-Security-Policy", 
                "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; font-src 'self' data:;");
            
            // 防止点击劫持
            httpResponse.setHeader("X-Frame-Options", "SAMEORIGIN");
            
            // 防止MIME类型嗅探
            httpResponse.setHeader("X-Content-Type-Options", "nosniff");
            
            // XSS保护
            httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
            
            // 引用策略
            httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
            
            // 权限策略
            httpResponse.setHeader("Permissions-Policy", "geolocation=(), microphone=(), camera=()");
            
            chain.doFilter(request, response);
        }
    }
    
    /**
     * CSRF保护过滤器实现
     */
    public static class CsrfProtectionFilter implements Filter {
        
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            
            String method = httpRequest.getMethod();
            String requestURI = httpRequest.getRequestURI();
            
            // 只对修改操作要求CSRF保护
            if ("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method) || "PATCH".equals(method)) {
                
                // 排除登录接口和公共API
                if (!isExcludedPath(requestURI)) {
                    
                    // 获取CSRF Token
                    String csrfToken = httpRequest.getHeader("X-CSRF-Token");
                    
                    if (csrfToken == null || !isValidCsrfToken(csrfToken)) {
                        log.warn("CSRF Token验证失败: {}, URI: {}", csrfToken, requestURI);
                        httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        httpResponse.getWriter().write("{\"code\":0,\"msg\":\"CSRF Token验证失败\"}");
                        return;
                    }
                }
            }
            
            chain.doFilter(request, response);
        }
        
        private boolean isExcludedPath(String requestURI) {
            // 排除的路径列表
            String[] excludedPaths = {
                "/user/login",
                "/user/register", 

                "/agent/login",
                "/agent/profile",    // 代理商资料接口
                "/agent/stats",      // 代理商统计接口
                "/agent/",           // 所有代理商相关接口
                "/admin/employee/login",
                "/auth/csrf-token",  // 更新路径
                "/auth/refresh",     // 更新路径
                "/auth/logout",      // 更新路径
                "/chatbot/message",  // 聊天机器人消息接口
                "/chatbot/health",   // 聊天机器人健康检查
                "/user/bookings/tour/calculate-price",  // 价格计算接口
                "/user/bookings/tour/create",  // 订单创建接口
                "/user/bookings/",   // 所有订单相关接口
                "/user/payments/",   // 所有支付相关接口
                "/user/day-tours",   // 一日游相关接口
                "/user/group-tours", // 团体游相关接口
                "/user/tours"        // 旅游产品相关接口
            };
            
            for (String path : excludedPaths) {
                if (requestURI.contains(path)) {
                    return true;
                }
            }
            return false;
        }
        
        private boolean isValidCsrfToken(String token) {
            // 简单的token验证（生产环境应该更严格）
            return token != null && token.length() > 10 && csrfTokenStore.containsValue(token);
        }
    }
    
    /**
     * 生成CSRF Token
     */
    public static String generateCsrfToken() {
        String token = UUID.randomUUID().toString().replace("-", "");
        String sessionId = "session_" + System.currentTimeMillis();
        csrfTokenStore.put(sessionId, token);
        
        // 清理过期的token（简单实现）
        if (csrfTokenStore.size() > 1000) {
            csrfTokenStore.clear();
        }
        
        return token;
    }
    
    /**
     * 验证CSRF Token
     */
    public static boolean validateCsrfToken(String token) {
        return token != null && csrfTokenStore.containsValue(token);
    }
    
    /**
     * 从请求中获取Cookie值 - 保留此方法用于获取refresh token
     */
    public static String getCookieValue(javax.servlet.http.HttpServletRequest request, String cookieName) {
        if (request.getCookies() != null) {
            for (javax.servlet.http.Cookie cookie : request.getCookies()) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
} 

