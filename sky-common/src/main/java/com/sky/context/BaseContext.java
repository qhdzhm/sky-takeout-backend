package com.sky.context;

public class BaseContext {

    public static ThreadLocal<Long> threadLocal = new ThreadLocal<>();
    public static ThreadLocal<String> usernameThreadLocal = new ThreadLocal<>();

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

}
