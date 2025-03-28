package com.sky.context;

/**
 * 基于ThreadLocal封装的工具类，用于保存和获取当前登录用户的id和其他信息
 */
public class BaseContext {

    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();
    private static ThreadLocal<String> usernameThreadLocal = new ThreadLocal<>();
    private static ThreadLocal<String> userTypeThreadLocal = new ThreadLocal<>();
    private static ThreadLocal<Long> agentIdThreadLocal = new ThreadLocal<>();

    /**
     * 设置当前用户id
     * @param id
     */
    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    /**
     * 获取当前用户id
     * @return
     */
    public static Long getCurrentId() {
        return threadLocal.get();
    }

    /**
     * 设置当前用户名
     * @param username
     */
    public static void setCurrentUsername(String username) {
        usernameThreadLocal.set(username);
    }

    /**
     * 获取当前用户名
     * @return
     */
    public static String getCurrentUsername() {
        return usernameThreadLocal.get();
    }

    /**
     * 设置当前用户类型
     * @param userType
     */
    public static void setCurrentUserType(String userType) {
        userTypeThreadLocal.set(userType);
    }

    /**
     * 获取当前用户类型
     * @return
     */
    public static String getCurrentUserType() {
        return userTypeThreadLocal.get();
    }

    /**
     * 设置当前代理商ID
     * @param agentId
     */
    public static void setCurrentAgentId(Long agentId) {
        agentIdThreadLocal.set(agentId);
    }

    /**
     * 获取当前代理商ID
     * @return
     */
    public static Long getCurrentAgentId() {
        return agentIdThreadLocal.get();
    }
    
    /**
     * 移除当前线程的所有信息
     */
    public static void removeAll() {
        threadLocal.remove();
        usernameThreadLocal.remove();
        userTypeThreadLocal.remove();
        agentIdThreadLocal.remove();
    }
} 