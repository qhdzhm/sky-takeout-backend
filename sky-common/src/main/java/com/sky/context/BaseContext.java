package com.sky.context;

public class BaseContext {

    public static ThreadLocal<Long> threadLocal = new ThreadLocal<>();
    public static ThreadLocal<String> usernameThreadLocal = new ThreadLocal<>();
    public static ThreadLocal<Long> agentIdThreadLocal = new ThreadLocal<>();
    public static ThreadLocal<String> userTypeThreadLocal = new ThreadLocal<>();
    public static ThreadLocal<Long> operatorIdThreadLocal = new ThreadLocal<>();

    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    public static Long getCurrentId() {
        return threadLocal.get();
    }

    public static void removeCurrentId() {
        threadLocal.remove();
    }

    public static void setCurrentUsername(String username) {
        usernameThreadLocal.set(username);
    }

    public static String getCurrentUsername() {
        return usernameThreadLocal.get();
    }

    public static void removeCurrentUsername() {
        usernameThreadLocal.remove();
    }
    
    public static void setCurrentAgentId(Long agentId) {
        agentIdThreadLocal.set(agentId);
    }

    public static Long getCurrentAgentId() {
        return agentIdThreadLocal.get();
    }

    public static void removeCurrentAgentId() {
        agentIdThreadLocal.remove();
    }
    
    public static void setCurrentUserType(String userType) {
        userTypeThreadLocal.set(userType);
    }

    public static String getCurrentUserType() {
        return userTypeThreadLocal.get();
    }

    public static void removeCurrentUserType() {
        userTypeThreadLocal.remove();
    }
    
    public static void setCurrentOperatorId(Long operatorId) {
        operatorIdThreadLocal.set(operatorId);
    }

    public static Long getCurrentOperatorId() {
        return operatorIdThreadLocal.get();
    }

    public static void removeCurrentOperatorId() {
        operatorIdThreadLocal.remove();
    }
    
    public static void removeAll() {
        threadLocal.remove();
        usernameThreadLocal.remove();
        agentIdThreadLocal.remove();
        userTypeThreadLocal.remove();
        operatorIdThreadLocal.remove();
    }
}
