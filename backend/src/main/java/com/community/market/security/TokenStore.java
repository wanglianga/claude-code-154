package com.community.market.security;

import com.community.market.common.BizException;
import com.community.market.domain.enums.UserRole;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 极简内存会话：登录返回 token，请求头 Authorization: Bearer {token}。
 * 单机演示足够；生产可替换为 JWT / Redis。
 */
@Component
public class TokenStore {

    private final Map<String, CurrentUser.SessionUser> sessions = new ConcurrentHashMap<>();

    public String issue(Long userId, String username, UserRole role) {
        String token = UUID.randomUUID().toString().replace("-", "");
        sessions.put(token, new CurrentUser.SessionUser(userId, username, role));
        return token;
    }

    public CurrentUser.SessionUser resolve(String token) {
        return token == null ? null : sessions.get(token);
    }

    public void requireRole(UserRole... roles) {
        CurrentUser.SessionUser user = CurrentUser.get();
        if (user == null) {
            throw new BizException("未登录");
        }
        for (UserRole role : roles) {
            if (user.role() == role) {
                return;
            }
        }
        throw new BizException("无权限执行该操作");
    }
}
