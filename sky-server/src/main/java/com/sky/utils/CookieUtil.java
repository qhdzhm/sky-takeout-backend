package com.sky.utils;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 统一的Cookie管理工具类
 * 解决多套Cookie设置导致的清理不完全问题
 */
@Slf4j
public class CookieUtil {
    
    /**
     * 设置Cookie到多个路径（用于代理商登录等场景）
     * @param response HTTP响应
     * @param name Cookie名称
     * @param value Cookie值
     * @param httpOnly 是否HttpOnly
     * @param maxAge 过期时间（秒）
     */
    public static void setCookieWithMultiplePaths(HttpServletResponse response, String name, String value, boolean httpOnly, int maxAge) {
        // 设置到不同路径的Cookie
        String[] paths = {"/", "/api", "/agent"};
        
        for (String path : paths) {
            setCookie(response, name, value, path, httpOnly, maxAge);
        }
        
        log.debug("已设置Cookie到多个路径: {} (maxAge: {})", name, maxAge);
    }
    
    /**
     * 设置单个Cookie
     * @param response HTTP响应
     * @param name Cookie名称
     * @param value Cookie值
     * @param path Cookie路径
     * @param httpOnly 是否HttpOnly
     * @param maxAge 过期时间（秒）
     */
    public static void setCookie(HttpServletResponse response, String name, String value, String path, boolean httpOnly, int maxAge) {
        if (httpOnly) {
            // 对于HttpOnly Cookie，使用Set-Cookie头部设置，确保兼容性
            response.addHeader("Set-Cookie", String.format("%s=%s; Path=%s; Max-Age=%d; HttpOnly; SameSite=Lax", 
                name, value, path, maxAge));
        } else {
            // 对于普通Cookie，使用标准方式
            Cookie cookie = new Cookie(name, value);
            cookie.setHttpOnly(false);
            cookie.setSecure(false); // 开发环境设为false，生产环境应设为true
            cookie.setPath(path);
            cookie.setMaxAge(maxAge);
            response.addCookie(cookie);
        }
    }
    
    /**
     * 清理Cookie（多路径版本）
     * @param response HTTP响应
     * @param cookieName Cookie名称
     */
    public static void clearCookieAllPaths(HttpServletResponse response, String cookieName) {
        String[] paths = {"/", "/api", "/agent"};
        
        log.info("清理Cookie: {} 在所有路径下", cookieName);
        
        for (String path : paths) {
            log.info("清理Cookie: {} 在路径: {}", cookieName, path);
            clearCookie(response, cookieName, path);
        }
        
        log.info("已完成清理Cookie: {} 在所有路径下", cookieName);
    }
    
    /**
     * 清理单个路径下的Cookie（增强版本，确保彻底清理）
     * @param response HTTP响应
     * @param cookieName Cookie名称
     * @param path Cookie路径
     */
    public static void clearCookie(HttpServletResponse response, String cookieName, String path) {
        try {
            // 方式1：清理HttpOnly Cookie（localhost域）
            String httpOnlyLocalhost = String.format("%s=; Path=%s; Domain=localhost; Max-Age=0; HttpOnly; SameSite=Lax", cookieName, path);
            response.addHeader("Set-Cookie", httpOnlyLocalhost);
            log.debug("设置HttpOnly清理Cookie(localhost): {}", httpOnlyLocalhost);
            
            // 方式2：清理HttpOnly Cookie（无域）
            String httpOnlyNoDomain = String.format("%s=; Path=%s; Max-Age=0; HttpOnly; SameSite=Lax", cookieName, path);
            response.addHeader("Set-Cookie", httpOnlyNoDomain);
            log.debug("设置HttpOnly清理Cookie(无域): {}", httpOnlyNoDomain);
            
            // 方式3：清理普通Cookie（localhost域）
            String normalLocalhost = String.format("%s=; Path=%s; Domain=localhost; Max-Age=0; SameSite=Lax", cookieName, path);
            response.addHeader("Set-Cookie", normalLocalhost);
            log.debug("设置普通清理Cookie(localhost): {}", normalLocalhost);
            
            // 方式4：清理普通Cookie（无域）
            String normalNoDomain = String.format("%s=; Path=%s; Max-Age=0; SameSite=Lax", cookieName, path);
            response.addHeader("Set-Cookie", normalNoDomain);
            log.debug("设置普通清理Cookie(无域): {}", normalNoDomain);
            
            // 方式5：使用Cookie对象作为额外保障
            Cookie cookie = new Cookie(cookieName, "");
            cookie.setPath(path);
            cookie.setMaxAge(0);
            cookie.setHttpOnly(false);
            cookie.setSecure(false);
            response.addCookie(cookie);
            log.debug("设置Cookie对象清理: name={}, path={}, maxAge=0", cookieName, path);
            
            log.debug("完成清理Cookie: {} 在路径: {} (使用5种方式)", cookieName, path);
        } catch (Exception e) {
            log.error("清理Cookie失败: {} 在路径: {} - {}", cookieName, path, e.getMessage());
        }
    }
    
    /**
     * 批量清理代理商相关的所有Cookie
     * @param response HTTP响应
     */
    public static void clearAllAgentCookies(HttpServletResponse response) {
        // 当前使用的Cookie（正确的双Token模式）
        String[] cookieNames = {"authToken", "refreshToken", "userInfo", "token", "jwt"};
        // 向后兼容：也清理可能存在的旧Token（如果之前设置过）
        String[] legacyCookieNames = {"agentToken"};
        
        log.info("开始清理代理商相关Cookie，当前{}个，兼容{}个", cookieNames.length, legacyCookieNames.length);
        
        // 清理当前使用的Cookie
        for (String cookieName : cookieNames) {
            log.info("正在清理当前Cookie: {}", cookieName);
            clearCookieAllPaths(response, cookieName);
        }
        
        // 清理可能存在的旧Cookie（向后兼容）
        for (String cookieName : legacyCookieNames) {
            log.info("正在清理兼容Cookie: {}", cookieName);
            clearCookieAllPaths(response, cookieName);
        }
        
        // 强制清理：添加JavaScript清理指令
        addJavaScriptCookieClearance(response);
        
        log.info("已完成清理所有代理商相关Cookie");
    }
    
    /**
     * 添加JavaScript清理指令（用于清理前端可访问的Cookie）
     * @param response HTTP响应
     */
    private static void addJavaScriptCookieClearance(HttpServletResponse response) {
        // 添加自定义头部，前端可以读取并执行清理
        response.addHeader("X-Clear-Cookies", "authToken,refreshToken,userInfo,token,jwt,agentToken");
        log.debug("已添加前端Cookie清理指令");
    }
    
    /**
     * 批量清理普通用户相关的所有Cookie
     * @param response HTTP响应
     */
    public static void clearAllUserCookies(HttpServletResponse response) {
        // 当前使用的Cookie（正确的双Token模式）
        String[] cookieNames = {"authToken", "refreshToken", "userInfo", "token", "jwt"};
        
        log.info("开始清理用户相关Cookie，共{}个", cookieNames.length);
        
        // 清理当前使用的Cookie
        for (String cookieName : cookieNames) {
            log.info("正在清理Cookie: {}", cookieName);
            clearCookieAllPaths(response, cookieName);
        }
        
        log.info("已完成清理所有用户相关Cookie");
    }
    
    /**
     * 从请求中获取Cookie值
     * @param request HTTP请求
     * @param cookieName Cookie名称
     * @return Cookie值，如果不存在返回null
     */
    public static String getCookieValue(javax.servlet.http.HttpServletRequest request, String cookieName) {
        if (request.getCookies() != null) {
            for (javax.servlet.http.Cookie cookie : request.getCookies()) {
                if (cookieName.equals(cookie.getName())) {
                    String value = cookie.getValue();
                    // 如果是userInfo Cookie，需要进行URL解码
                    if ("userInfo".equals(cookieName) && value != null) {
                        try {
                            return java.net.URLDecoder.decode(value, "UTF-8");
                        } catch (Exception e) {
                            log.warn("解码userInfo Cookie失败: {}", e.getMessage());
                            return value; // 返回原始值作为fallback
                        }
                    }
                    return value;
                }
            }
        }
        return null;
    }
    
    /**
     * 设置用户信息Cookie（非HttpOnly，供前端读取）- 多路径版本
     * @param response HTTP响应
     * @param userInfoJson 用户信息JSON字符串
     * @param maxAge 过期时间（秒）
     */
    public static void setUserInfoCookie(HttpServletResponse response, String userInfoJson, int maxAge) {
        try {
            // 对JSON字符串进行URL编码，避免双引号等特殊字符导致Cookie设置失败
            String encodedUserInfo = java.net.URLEncoder.encode(userInfoJson, "UTF-8");
            setCookieWithMultiplePaths(response, "userInfo", encodedUserInfo, false, maxAge);
            
            log.debug("已设置用户信息Cookie到多个路径");
        } catch (Exception e) {
            log.error("设置用户信息Cookie失败", e);
        }
    }
} 