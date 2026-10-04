package com.community.market.security;

import com.community.market.domain.enums.UserRole;

/** 当前登录用户上下文（线程内有效） */
public class CurrentUser {
    private static final ThreadLocal<SessionUser> HOLDER = new ThreadLocal<>();

    public static void set(SessionUser user) {
        HOLDER.set(user);
    }

    public static SessionUser get() {
        return HOLDER.get();
    }

    public static Long userId() {
        SessionUser u = HOLDER.get();
        return u == null ? null : u.userId();
    }

    public static void clear() {
        HOLDER.remove();
    }

    public record SessionUser(Long userId, String username, UserRole role) {
    }
}
