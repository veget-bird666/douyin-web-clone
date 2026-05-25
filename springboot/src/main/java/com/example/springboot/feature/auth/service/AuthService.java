package com.example.springboot.feature.auth.service;

import com.example.springboot.exception.CustomerException;
import com.example.springboot.feature.auth.entity.User;
import com.example.springboot.feature.auth.mapper.UserMapper;
import com.example.springboot.feature.auth.util.JwtUtil;
import jakarta.annotation.Resource;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Service
public class AuthService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.-]+@[\\w-]+(\\.[\\w-]+)+$");

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final ConcurrentHashMap<String, CodeRecord> emailCodeStore = new ConcurrentHashMap<>();

    @Resource
    private UserMapper userMapper;

    @Resource
    private JwtUtil jwtUtil;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${douyin.email-code-expire-seconds:300}")
    private int codeExpireSeconds;

    @Value("${douyin.email-code-cooldown-seconds:30}")
    private int codeCooldownSeconds;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${douyin.mail-from-name:抖音精选}")
    private String mailFromName;

    @Value("${douyin.mail-from-address:}")
    private String mailFromAddress;

    @Value("${douyin.mail-smtp-domain:}")
    private String mailSmtpDomain;

    @Value("${douyin.mail-dev-log:false}")
    private boolean mailDevLog;

    public Map<String, Object> sendCode(String email) {
        String normalized = normalizeEmail(email);
        long now = System.currentTimeMillis();
        CodeRecord existing = emailCodeStore.get(normalized);
        if (existing != null && now - existing.sentAt < codeCooldownSeconds * 1000L) {
            long remain = (codeCooldownSeconds * 1000L - (now - existing.sentAt) + 999) / 1000;
            throw new CustomerException("429", "发送过于频繁，请在 " + remain + "s 后重试");
        }

        String code = String.valueOf((int) (100000 + Math.random() * 900000));
        sendVerifyEmail(normalized, code);

        emailCodeStore.put(normalized, new CodeRecord(code, now, now + codeExpireSeconds * 1000L));

        Map<String, Object> data = new HashMap<>();
        data.put("email", normalized);
        data.put("cooldownSeconds", codeCooldownSeconds);
        return data;
    }

    public Map<String, Object> register(String email, String password, String code) {
        String normalized = normalizeEmail(email);
        validatePassword(password);
        verifyCode(normalized, code);

        if (userMapper.selectByEmail(normalized) != null) {
            throw new CustomerException("409", "该邮箱已注册");
        }

        User user = new User();
        user.setUserId(UUID.randomUUID().toString().replace("-", ""));
        user.setEmail(normalized);
        user.setNickname(normalized.split("@")[0]);
        user.setPasswordHash(passwordEncoder.encode(password));

        userMapper.insert(user);
        emailCodeStore.remove(normalized);

        Map<String, Object> data = new HashMap<>();
        data.put("token", jwtUtil.createToken(user.getId(), user.getUserId(), user.getEmail()));
        data.put("id", user.getId());
        data.put("userId", user.getUserId());
        data.put("email", user.getEmail());
        data.put("nickname", user.getNickname());
        return data;
    }

    public Map<String, Object> login(String email, String password) {
        String normalized = normalizeEmail(email);
        User user = userMapper.selectByEmail(normalized);
        if (user == null || user.getPasswordHash() == null) {
            throw new CustomerException("401", "账号或密码错误");
        }
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new CustomerException("401", "账号或密码错误");
        }

        String token = jwtUtil.createToken(user.getId(), user.getUserId(), user.getEmail());

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("userId", user.getUserId());
        userInfo.put("email", user.getEmail());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("avatar", user.getAvatar());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", userInfo);
        return data;
    }

    private void verifyCode(String email, String code) {
        if (code == null || code.isBlank()) {
            throw new CustomerException("400", "邮箱、密码、验证码不能为空");
        }
        CodeRecord record = emailCodeStore.get(email);
        if (record == null) {
            throw new CustomerException("400", "请先获取验证码");
        }
        if (System.currentTimeMillis() > record.expiresAt) {
            emailCodeStore.remove(email);
            throw new CustomerException("400", "验证码已过期，请重新获取");
        }
        if (!record.code.equals(code.trim())) {
            throw new CustomerException("400", "验证码错误");
        }
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new CustomerException("400", "邮箱不能为空");
        }
        String normalized = email.trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new CustomerException("400", "邮箱格式不正确");
        }
        return normalized;
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 6) {
            throw new CustomerException("400", "密码至少6位");
        }
        if (password.length() > 32) {
            throw new CustomerException("400", "密码不能超过32位");
        }
    }

    private void sendVerifyEmail(String email, String code) {
        if (mailSender == null || mailUsername == null || mailUsername.isBlank()) {
            if (mailDevLog) {
                System.out.println("[DEV] 验证码 " + email + " -> " + code);
                return;
            }
            throw new CustomerException("500", "邮件服务未配置，请在 application.yml 配置 spring.mail，或开启 douyin.mail-dev-log");
        }

        String fromEmail = resolveFromAddress();
        if (!fromEmail.contains("@")) {
            throw new CustomerException("500", "发件人必须是完整邮箱，请配置 douyin.mail-from-address");
        }

        try {
            int minutes = Math.max(1, (codeExpireSeconds + 59) / 60);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, mailFromName);
            helper.setTo(email);
            helper.setSubject("抖音精选验证码");
            helper.setText(buildPlainText(code, minutes), buildHtml(code, minutes));
            mailSender.send(message);
        } catch (CustomerException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomerException("500", "验证码发送失败: " + e.getMessage());
        }
    }

    private String resolveFromAddress() {
        if (mailFromAddress != null && !mailFromAddress.isBlank() && mailFromAddress.contains("@")) {
            return mailFromAddress.trim();
        }
        String user = mailUsername != null ? mailUsername.trim() : "";
        if (user.contains("@")) {
            return user;
        }
        String domain = mailSmtpDomain != null ? mailSmtpDomain.trim() : "";
        return domain.isEmpty() ? user : user + "@" + domain;
    }

    private String buildPlainText(String code, int minutes) {
        return "【抖音精选】您的验证码是 " + code + "，请在 " + minutes + " 分钟内完成登录或注册，请勿泄露给他人。";
    }

    private String buildHtml(String code, int minutes) {
        return """
            <div style="max-width:600px;margin:0 auto;padding:20px;font-family:Arial,'PingFang SC','Microsoft YaHei',sans-serif;border:1px solid #e8e8e8;border-radius:12px;background:#fff;">
              <div style="text-align:center;padding:24px 0 16px;border-bottom:1px solid #f0f0f0;">
                <div style="display:inline-block;width:44px;height:44px;line-height:44px;border-radius:10px;background:linear-gradient(135deg,#25f4ee,#fe2c55);color:#fff;font-size:18px;font-weight:bold;">抖</div>
                <h1 style="color:#161823;font-size:22px;margin:12px 0 4px;">抖音精选 · 验证码</h1>
                <p style="color:#888;font-size:13px;margin:0;">用于登录或注册你的抖音精选账号</p>
              </div>
              <div style="background:#f8f9fb;padding:32px 24px;text-align:center;border-radius:8px;margin:20px 0;">
                <p style="color:#666;font-size:14px;margin:0 0 16px;">您正在获取邮箱验证码，请在下方的输入框中填写：</p>
                <div style="font-size:38px;font-weight:bold;color:#fe2c55;letter-spacing:8px;margin:16px 0;">%s</div>
                <p style="color:#999;font-size:12px;margin:0;">请在 <strong style="color:#161823;">%d 分钟</strong> 内完成验证，过期后需重新获取</p>
              </div>
              <div style="margin-top:16px;padding:16px;background:#fff8e6;border-radius:8px;border-left:4px solid #ffc107;">
                <p style="color:#856404;font-size:12px;margin:0 0 8px;font-weight:bold;">安全提示</p>
                <ul style="color:#856404;font-size:12px;margin:0;padding-left:18px;line-height:1.7;">
                  <li>抖音精选工作人员不会向您索要验证码</li>
                  <li>请勿将验证码转发给他人或透露给第三方</li>
                  <li>如非本人操作，请忽略此邮件</li>
                </ul>
              </div>
              <hr style="border:0;border-top:1px solid #eee;margin:24px 0 16px;">
              <p style="color:#bbb;font-size:11px;text-align:center;margin:0;">此邮件由抖音精选系统自动发送，请勿直接回复</p>
            </div>
            """.formatted(code, minutes);
    }

    private record CodeRecord(String code, long sentAt, long expiresAt) {}
}
