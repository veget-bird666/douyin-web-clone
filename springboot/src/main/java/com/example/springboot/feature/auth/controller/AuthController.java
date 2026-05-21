package com.example.springboot.feature.auth.controller;

import com.example.springboot.common.Result;
import com.example.springboot.feature.auth.service.AuthService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Resource
    private AuthService authService;

    @PostMapping("/send-code")
    public Result sendCode(@RequestBody Map<String, String> body) {
        String email = body != null ? body.get("email") : null;
        return Result.success(authService.sendCode(email), "验证码已发送，请注意查收邮箱");
    }

    @PostMapping("/register")
    public Result register(@RequestBody Map<String, String> body) {
        if (body == null) {
            return Result.error("400", "邮箱、密码、验证码不能为空");
        }
        String email = body.get("email");
        String password = body.get("password");
        String code = body.get("code");
        if (email == null || password == null || code == null) {
            return Result.error("400", "邮箱、密码、验证码不能为空");
        }
        return Result.success(authService.register(email, password, code), "注册成功");
    }

    @PostMapping("/login")
    public Result login(@RequestBody Map<String, String> body) {
        if (body == null) {
            return Result.error("400", "邮箱和密码不能为空");
        }
        String email = body.get("email");
        String password = body.get("password");
        if (email == null || password == null) {
            return Result.error("400", "邮箱和密码不能为空");
        }
        return Result.success(authService.login(email, password), "登录成功");
    }
}
