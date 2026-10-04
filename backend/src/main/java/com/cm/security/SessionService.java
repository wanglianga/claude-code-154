package com.cm.security;

import com.cm.domain.User;
import com.cm.repo.UserRepository;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 极简会话：登录成功后颁发不透明 token，服务重启后需重新登录。
 * 仅用于本演示平台的角色区分（摊主/工作人员/管理员）。
 */
@Component
public class SessionService {

    private final Map<String, Long> tokenToUser = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    private final UserRepository userRepository;

    public SessionService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String createToken(Long userId) {
        byte[] bytes = new byte[24];
        random.nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        String token = sb.toString();
        tokenToUser.put(token, userId);
        return token;
    }

    public Optional<User> resolve(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        Long userId = tokenToUser.get(token);
        if (userId == null) {
            return Optional.empty();
        }
        return userRepository.findById(userId);
    }
}
