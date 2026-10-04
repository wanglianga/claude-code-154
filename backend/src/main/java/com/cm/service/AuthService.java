package com.cm.service;

import com.cm.domain.User;
import com.cm.repo.UserRepository;
import com.cm.security.SessionService;
import com.cm.web.ApiException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final SessionService sessions;

    public AuthService(UserRepository userRepository, SessionService sessions) {
        this.userRepository = userRepository;
        this.sessions = sessions;
    }

    public Map<String, Object> login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(401, "账号或密码错误"));
        if (!user.getPassword().equals(password)) {
            throw new ApiException(401, "账号或密码错误");
        }
        String token = sessions.createToken(user.getId());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", profile(user));
        return result;
    }

    public Map<String, Object> profile(User user) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", user.getId());
        m.put("username", user.getUsername());
        m.put("name", user.getName());
        m.put("phone", user.getPhone());
        m.put("role", user.getRole());
        m.put("roleLabel", switch (user.getRole()) {
            case ADMIN -> "社区管理员";
            case STAFF -> "现场工作人员";
            case VENDOR -> "摊主";
        });
        m.put("creditScore", user.getCreditScore());
        m.put("totalDonationYuan", user.getTotalDonationCents() / 100.0);
        m.put("foodRiskFlag", user.getFoodRiskFlag());
        return m;
    }
}
