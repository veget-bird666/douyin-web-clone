package com.example.springboot.aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger("API_LOG");

    @Around("execution(* com.example.springboot.controller..*.*(..))")
    public Object logApiCall(ProceedingJoinPoint pjp) throws Throwable {
        // 获取 HTTP 请求
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return pjp.proceed();
        }
        HttpServletRequest request = attrs.getRequest();

        // 跳过文件上传的详细入参日志（太大）
        String contentType = request.getContentType();
        boolean isMultipart = contentType != null && contentType.startsWith("multipart/");

        // 基础信息
        String httpMethod = request.getMethod();
        String requestUri = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullPath = queryString != null ? requestUri + "?" + queryString : requestUri;

        // 用户信息
        String userId = getCurrentUserId();

        // 方法签名
        String methodName = pjp.getTarget().getClass().getSimpleName() + "." + pjp.getSignature().getName();

        // 入参
        String input;
        if (isMultipart) {
            input = "[multipart file upload]";
        } else {
            Object[] args = pjp.getArgs();
            input = (args != null && args.length > 0) ? truncate(Arrays.deepToString(args), 500) : "[]";
        }

        long start = System.currentTimeMillis();

        try {
            Object result = pjp.proceed();
            long duration = System.currentTimeMillis() - start;

            log.info("[{} {}] userId={} | {} | input={} | output={} | {}ms",
                    httpMethod, fullPath, userId, methodName, input,
                    truncate(String.valueOf(result), 300), duration);

            return result;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - start;

            log.error("[{} {}] userId={} | {} | input={} | error={} | {}ms",
                    httpMethod, fullPath, userId, methodName, input, e.getMessage(), duration);

            throw e;
        }
    }

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal())) {
            Object credentials = auth.getCredentials();
            return credentials != null ? credentials.toString() : "unknown";
        }
        return "anonymous";
    }

    private String truncate(String str, int maxLen) {
        if (str == null) return "null";
        if (str.length() <= maxLen) return str;
        return str.substring(0, maxLen) + "...(" + str.length() + " chars total)";
    }
}
