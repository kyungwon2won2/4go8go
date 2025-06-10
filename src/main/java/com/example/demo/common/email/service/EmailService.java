package com.example.demo.common.email.service;

import com.example.demo.common.email.dto.EmailRequest;
import com.example.demo.common.email.dto.EmailResponse;
import com.example.demo.common.email.model.EmailVerification;
import com.example.demo.common.exception.custom.CustomException;
import com.example.demo.common.stringcode.ErrorCode;
import com.example.demo.mapper.EmailVerificationMapper;
import com.example.demo.mapper.UserMapper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final UserMapper userMapper;
    private final EmailVerificationMapper emailVerificationMapper;

    // 인증 코드 생성 + 메일 전송 (이메일 대소문자 무시)
    @Transactional
    public void generateAndSendCode(String email) {
        String normalizedEmail = email.toLowerCase();

        if (userMapper.existsByEmail(normalizedEmail)) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL_EXIST);
        }

        // 1. 기존 인증 요청 삭제
        emailVerificationMapper.deleteByEmail(normalizedEmail);

        // 2. 새 인증 코드 생성 및 저장
        String code = generateRandomCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(3);

        EmailVerification verification = new EmailVerification(normalizedEmail, code, expiresAt);
        emailVerificationMapper.insert(verification);

        // 3. 이메일 전송
        sendVerificationEmail(normalizedEmail, code);

        // 테스트시 로그로 회원가입
        log.info(">>>>  code : " + code);
    }


    // 이메일 코드 검증(코드 사용 후 삭제, 시도 횟수 제한, 타이밍 공격 대비, 이메일 대소문자 무시)
    @Transactional
    public ResponseEntity<EmailResponse> verifyCode(EmailRequest emailRequest) {
        String normalizedEmail = emailRequest.email().toLowerCase();
        String code = emailRequest.code();
        EmailVerification verification = emailVerificationMapper.selectByEmail(normalizedEmail);

        if (verification == null) {
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_NOT_FOUND);
        }

        // 제한시간 만료 시
        if (verification.isExpired()) {
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_EXPIRED_OR_LIMITED);
        }

        // 인증번호 검증
        boolean matched = MessageDigest.isEqual(
                verification.getCode().getBytes(StandardCharsets.UTF_8),
                code.getBytes(StandardCharsets.UTF_8)
        );

        // 인증번호가 일치하지 않을 시
        if (!matched) {
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_CODE_MISMATCH);
        }

        emailVerificationMapper.deleteByEmail(normalizedEmail);

        return ResponseEntity.ok(new EmailResponse("이메일 인증이 성공했습니다.", true));
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

}

