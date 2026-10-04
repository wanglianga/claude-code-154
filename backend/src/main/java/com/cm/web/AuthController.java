package com.cm.web;

import com.cm.domain.User;
import com.cm.security.CurrentUser;
import com.cm.service.AuthService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        return authService.login(body.get("username"), body.get("password"));
    }

    @GetMapping("/me")
    public Map<String, Object> me() {
        User u = Roles.user();
        return authService.profile(u);
    }
}
