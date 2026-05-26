package com.example.springboot.config;

import com.example.springboot.feature.auth.util.JwtUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Resource
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        String authHeader = request.getHeader("Authorization");

        log.debug("[JwtAuth] {} | AuthHeader={}", uri, authHeader != null ? "present" : "null");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                var claims = jwtUtil.parse(token);
                String email = claims.get("email", String.class);
                String publicUserId = claims.get("userId", String.class);
                log.debug("[JwtAuth] 解析成功 - email={}, userId={}", email, publicUserId);
                if (email != null) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(email, publicUserId, Collections.emptyList());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info("[JwtAuth] 已设置认证 - uri={}, userId={}", uri, publicUserId);
                }
            } catch (Exception e) {
                log.warn("[JwtAuth] Token解析失败: {} - {}", uri, e.getMessage());
            }
        } else {
            log.debug("[JwtAuth] 无Authorization头，跳过认证 - uri={}", uri);
        }
        filterChain.doFilter(request, response);
    }
}