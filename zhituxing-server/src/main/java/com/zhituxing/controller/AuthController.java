package com.zhituxing.controller;

import com.zhituxing.common.Result;
import com.zhituxing.dto.LoginRequest;
import com.zhituxing.dto.RegisterRequest;
import com.zhituxing.dto.TokenResponse;
import com.zhituxing.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** 3.1 用户注册（公开） */
    @PostMapping("/users")
    public Result<Map<String, Long>> register(@Valid @RequestBody RegisterRequest request) {
        return Result.ok(Map.of("userId", authService.register(request)));
    }

    /** 3.2 用户登录（公开） */
    @PostMapping("/auth/token")
    public Result<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    /** 3.3 退出登录（无状态 JWT，服务端无需删除令牌） */
    @DeleteMapping("/auth/token")
    public Result<Void> logout() {
        return Result.ok();
    }
}
