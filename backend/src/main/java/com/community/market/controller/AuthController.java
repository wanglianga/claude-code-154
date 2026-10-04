package com.community.market.controller;

import com.community.market.common.ApiResult;
import com.community.market.domain.entity.User;
import com.community.market.dto.AuthDtos;
import com.community.market.security.CurrentUser;
import com.community.market.service.CreditService;
import com.community.market.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final CreditService creditService;

    @PostMapping("/auth/login")
    public ApiResult<AuthDtos.LoginResponse> login(@Valid @RequestBody AuthDtos.LoginRequest req) {
        return ApiResult.ok(userService.login(req.username(), req.password()));
    }

    /** 摊主自助注册 */
    @PostMapping("/auth/register")
    public ApiResult<AuthDtos.UserResponse> register(@Valid @RequestBody AuthDtos.RegisterRequest req) {
        User u = userService.register(req);
        return ApiResult.ok(userService.toResponse(u));
    }

    @GetMapping("/auth/me")
    public ApiResult<Map<String, Object>> me() {
        return ApiResult.ok(Map.of(
                "userId", CurrentUser.userId(),
                "username", CurrentUser.get().username(),
                "role", CurrentUser.get().role().name()));
    }

    /** 摊主查看自己的信用明细 */
    @GetMapping("/my/credit")
    public ApiResult<Object> myCredit() {
        Long uid = CurrentUser.userId();
        return ApiResult.ok(Map.of("logs", creditService.logs(uid)));
    }
}
