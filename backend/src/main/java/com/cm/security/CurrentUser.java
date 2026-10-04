package com.cm.security;

import com.cm.domain.User;

/** 当前请求登录用户的 ThreadLocal 上下文，由 AuthInterceptor 在请求开始时写入 */
public final class CurrentUser {

    private static final ThreadLocal<User> HOLDER = new ThreadLocal<>();

    private CurrentUser() {}

    public static void set(User user) { HOLDER.set(user); }

    public static User get() { return HOLDER.get(); }

    public static Long id() {
        User u = HOLDER.get();
        return u == null ? null : u.getId();
    }

    public static void clear() { HOLDER.remove(); }
}
