package com.example.demo.common.email.service;

import com.example.demo.common.exception.custom.EmailAlreadyExistsException;
import com.example.demo.mapper.UserMapper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final UserMapper userMapper;

    private final Map<String, EmailVerificationInfo> verificationStorage = new ConcurrentHashMap<>();

    // 인증 코드 생성 + 메일 전송
    public void generateAndSendCode(String email) {
        if (userMapper.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("이미 등록된 이메일입니다.");
        }
        String code = generateRandomCode();
        verificationStorage.put(email, new EmailVerificationInfo(code, LocalDateTime.now().plusMinutes(3)));
        sendVerificationEmail(email, code);
    }

    // 인증 코드 검증
    public boolean verifyCode(String email, String code) {
        EmailVerificationInfo info = verificationStorage.get(email);
        if (info == null || LocalDateTime.now().isAfter(info.getExpiresAt())) {
            return false;
        }
        return info.getCode().equals(code);
    }

    // 메일 전송
    private void sendVerificationEmail(String toEmail, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    "UTF-8"
            );

            helper.setFrom("your_email@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject("[서비스명] 이메일 인증 코드");

            Context context = new Context();
            context.setVariable("code", code);

            String htmlContent = templateEngine.process("email/verify-email", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("이메일 전송 중 오류 발생", e);
        }
    }

    private String generateRandomCode() {
        return String.valueOf((int)(Math.random() * 900000) + 100000); // 6자리 숫자
    }

    @Data
    @AllArgsConstructor
    private static class EmailVerificationInfo {
        private String code;
        private LocalDateTime expiresAt;
    }
}

