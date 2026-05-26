# Swagger UI "Failed to Fetch" / CORS 错误排查记录

## 错误现象

Swagger UI 中点击 "Try it out" 后报错：

```
Failed to fetch.
Possible Reasons:
  CORS
  Network Failure
  URL scheme must be "http" or "https" for CORS request.
```

后端控制台显示请求已到达，返回 401 或 403，但 Swagger UI 无法正常显示错误信息。

---

## 根因一：SecurityConfig 未返回 JSON

### 问题

Spring Security 拒绝未认证请求时，默认返回 **HTML 页面**（而非 JSON）。Swagger UI 期望 JSON 响应，解析 HTML 失败后显示 "Failed to fetch"。

### 日志特征

```
Http403ForbiddenEntryPoint: Pre-authenticated entry point called. Rejecting access
```

响应是 HTML，不是 `{"code":"401","msg":"..."}` 格式。

### 解决

在 `SecurityConfig` 中配置 `exceptionHandling`，让 401/403 返回 JSON：

```java
.exceptionHandling(ex -> ex
    .authenticationEntryPoint((request, response, authException) -> {
        response.setContentType("application/json;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        PrintWriter writer = response.getWriter();
        writer.write("{\"code\":\"401\",\"msg\":\"未登录或token已过期\"}");
    })
    .accessDeniedHandler((request, response, accessDeniedException) -> {
        response.setContentType("application/json;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        PrintWriter writer = response.getWriter();
        writer.write("{\"code\":\"403\",\"msg\":\"无权限访问\"}");
    })
)
```

---

## 根因二：OpenApiConfig 未绑定 SecurityRequirement

### 问题

OpenAPI 中只定义了 security scheme（`bearerAuth`），但没有通过 `addSecurityItem()` 全局绑定到接口上。导致 Swagger UI 的 "Authorize" 按钮形同虚设——用户填了 token，但实际请求中 **不会带上 `Authorization` 头**。

### 日志特征

```
>>> POST /video/upload | Origin=http://localhost:8080 | Auth=null
```

明明在 Swagger UI 里点了 Authorize 并填了 token，但 `Auth=null`。

### 解决

在 `OpenApiConfig` 的 OpenAPI bean 中添加全局 security 要求：

```java
return new OpenAPI()
        .components(new Components()
                .addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("输入JWT token")))
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
```

关键就是这行：

```java
.addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
```

---

## 根因三（次要）：CORS 与 Security 的协同问题

### 问题

`WebMvcConfigurer.addCorsMappings()` 只在请求到达 Controller 时加 CORS 头。但 Security 在 Controller 之前就拦截了请求（401/403），导致响应不带 CORS 头，浏览器报 CORS 错误。

### 解决

在 `CorsConfig` 中额外暴露一个 `CorsConfigurationSource` bean，Spring Security 会自动检测并使用它，确保 Security 层的所有响应都带上 CORS 头：

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOriginPatterns(List.of("*"));
    config.setAllowedMethods(List.of("*"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);
    config.setMaxAge(3600L);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```

---

## 排查工具

如果再次遇到类似问题，可以按以下步骤排查：

1. **查看后端日志** — 确认请求是否到达后端、返回的 HTTP 状态码
2. **检查 `Auth` 字段** — 日志中 `Auth=null` 表示没带 token，`Auth=present` 表示有 token
3. **检查响应状态码** — `→ 200` 成功，`→ 401` 未认证，`→ 403` 无权限
4. **用 curl 直测** — 避免浏览器/Swagger UI 的干扰：

   ```bash
   # 登录获取 token
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email":"your@email.com","password":"yourpwd"}'

   # 带 token 调用接口
   curl -X POST http://localhost:8080/video/upload \
     -H "Authorization: Bearer <token>" \
     -F "file=@test.mp4" \
     -F "title=test"
   ```

5. **临时加 `CorsLoggingFilter`** — 在 `config/` 下放一个 `@Order(Integer.MIN_VALUE)` 的 `Filter`，打印每个请求的方法、URI、Origin、Auth 头，以及响应状态码（见 `CorsLoggingFilter.java` 备份）
