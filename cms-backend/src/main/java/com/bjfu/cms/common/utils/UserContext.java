package com.bjfu.cms.common.utils;

public class UserContext {
    private static final ThreadLocal<Integer> userHolder = new ThreadLocal<>();

    // cache role to avoid repeated DB lookups
    private static final ThreadLocal<String> roleHolder = new ThreadLocal<>();

    public static void setUserId(Integer userId) {
        userHolder.set(userId);
    }

    public static Integer getUserId() {
        return userHolder.get();
    }

    public static void remove() {
        userHolder.remove();
        roleHolder.remove();
    }

    public static void setUserRole(String role) {
        roleHolder.set(role);
    }

    public static String getUserRole() {
        return roleHolder.get();
    }
}