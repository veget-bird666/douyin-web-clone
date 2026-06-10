package com.example.springboot.aspect;

import com.example.springboot.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger("API_LOG");
    private static final int OUTPUT_MAX_LEN = 300;
    private static final int INPUT_MAX_LEN = 500;

    @Around("execution(* com.example.springboot..*Controller.*(..))")
    public Object logApiCall(ProceedingJoinPoint pjp) throws Throwable {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return pjp.proceed();
        }

        HttpServletRequest request = attrs.getRequest();
        String httpMethod = request.getMethod();
        String requestUri = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullPath = queryString != null ? requestUri + "?" + queryString : requestUri;
        String userId = getCurrentUserId();
        String methodName = pjp.getTarget().getClass().getSimpleName() + "." + pjp.getSignature().getName();
        String input = formatInput(pjp.getArgs(), request.getContentType());

        long start = System.currentTimeMillis();

        try {
            Object result = pjp.proceed();
            long duration = System.currentTimeMillis() - start;

            log.info("[{} {}] userId={} | {} | input={} | output={} | {}ms",
                    httpMethod, fullPath, userId, methodName, input,
                    formatOutput(result), duration);

            return result;
        } catch (Throwable e) {
            long duration = System.currentTimeMillis() - start;

            log.error("[{} {}] userId={} | {} | input={} | error={} | {}ms",
                    httpMethod, fullPath, userId, methodName, input, e.getMessage(), duration);

            throw e;
        }
    }

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            return "anonymous";
        }
        Object credentials = auth.getCredentials();
        if (credentials != null && !credentials.toString().isBlank()) {
            return credentials.toString();
        }
        return String.valueOf(auth.getPrincipal());
    }

    private String formatInput(Object[] args, String contentType) {
        if (contentType != null && contentType.startsWith("multipart/")) {
            return "[multipart file upload]";
        }
        if (args == null || args.length == 0) {
            return "[]";
        }
        String joined = Stream.of(args)
                .map(this::formatArg)
                .collect(Collectors.joining(", ", "[", "]"));
        return truncate(joined, INPUT_MAX_LEN);
    }

    private String formatArg(Object arg) {
        if (arg == null) {
            return "null";
        }
        if (arg instanceof MultipartFile) {
            return "[multipart file upload]";
        }
        if (arg instanceof HttpServletRequest || arg instanceof HttpServletResponse) {
            return "[servlet]";
        }
        if (arg instanceof Map<?, ?> map) {
            return map.toString();
        }
        if (arg instanceof Iterable<?> iterable) {
            return iterable.toString();
        }
        if (isSimpleValue(arg)) {
            return String.valueOf(arg);
        }
        return formatRequestBody(arg);
    }

    private boolean isSimpleValue(Object arg) {
        return arg instanceof CharSequence
                || arg instanceof Number
                || arg instanceof Boolean
                || arg.getClass().isEnum();
    }

    private String formatRequestBody(Object arg) {
        try {
            StringBuilder sb = new StringBuilder(arg.getClass().getSimpleName()).append("(");
            boolean first = true;
            for (Field field : arg.getClass().getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                field.setAccessible(true);
                if (!first) {
                    sb.append(", ");
                }
                first = false;
                String name = field.getName();
                Object value = field.get(arg);
                if (isSensitiveField(name)) {
                    sb.append(name).append("=***");
                } else {
                    sb.append(name).append("=").append(value);
                }
            }
            return sb.append(")").toString();
        } catch (Exception e) {
            return arg.getClass().getSimpleName();
        }
    }

    private boolean isSensitiveField(String name) {
        return "password".equals(name) || "code".equals(name) || "token".equals(name);
    }

    private String formatOutput(Object result) {
        if (result == null) {
            return "null";
        }
        if (result instanceof Result r) {
            String dataSummary = summarizeData(r.getData());
            String formatted = "Result(code=" + r.getCode()
                    + ", msg=" + r.getMsg()
                    + ", data=" + dataSummary + ")";
            return truncate(formatted, OUTPUT_MAX_LEN);
        }
        return truncate(String.valueOf(result), OUTPUT_MAX_LEN);
    }

    private String summarizeData(Object data) {
        if (data == null) {
            return "null";
        }
        if (data instanceof Map<?, ?> map) {
            if (map.containsKey("token")) {
                return "{token=***, ...}";
            }
        }
        return String.valueOf(data);
    }

    private String truncate(String str, int maxLen) {
        if (str == null) {
            return "null";
        }
        if (str.length() <= maxLen) {
            return str;
        }
        return str.substring(0, maxLen) + "...(" + str.length() + " chars total)";
    }
}
