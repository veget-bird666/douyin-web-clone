package com.example.springboot.feature.auth.controller;

import com.example.springboot.common.Result;
import com.example.springboot.feature.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证管理")
public class AuthController {

    @Resource
    private AuthService authService;

    @PostMapping("/send-code")
    @Operation(summary = "发送邮箱验证码")
    public Result sendCode(@RequestBody SendCodeRequest request) {
        return Result.success(authService.sendCode(request.getEmail()), "验证码已发送，请注意查收邮箱");
    }

    @PostMapping("/register")
    @Operation(summary = "注册")
    public Result register(@RequestBody RegisterRequest request) {
        return Result.success(authService.register(request.getEmail(), request.getPassword(), request.getCode()), "注册成功");
    }

    @PostMapping("/login")
    @Operation(summary = "登录")
    public Result login(@RequestBody LoginRequest request) {
        return Result.success(authService.login(request.getEmail(), request.getPassword()), "登录成功");
    }

    public static class SendCodeRequest {
        private String email;
        public SendCodeRequest() {}
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class RegisterRequest {
        private String email;
        private String password;
        private String code;
        public RegisterRequest() {}
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }

    public static class LoginRequest {
        private String email;
        private String password;
        public LoginRequest() {}
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
