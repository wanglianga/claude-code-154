package com.cm.web;

import com.cm.domain.User;
import com.cm.domain.UserRole;
import com.cm.dto.Views;
import com.cm.repo.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserRepository users;

    public UserController(UserRepository users) {
        this.users = users;
    }

    /** 摊主信用榜/名单：管理员查看信用、历史捐赠、食品风险 */
    @GetMapping("/vendors")
    public List<Map<String, Object>> vendors() {
        Roles.requireAdmin();
        return users.findByRole(UserRole.VENDOR).stream()
                .sorted((a, b) -> b.getCreditScore().compareTo(a.getCreditScore()))
                .map(Views::user).toList();
    }
}
