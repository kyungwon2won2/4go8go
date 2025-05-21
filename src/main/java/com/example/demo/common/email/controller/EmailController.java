package com.example.demo.common.email.controller;

import com.example.demo.common.email.dto.EmailRequest;
import com.example.demo.common.email.dto.EmailResponse;
import com.example.demo.common.email.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
@Slf4j
public class EmailController {

    private final EmailService emailService;

    // 인증 코드 요청
    @PostMapping("/send")
    public ResponseEntity<?> sendVerificationCode(@RequestBody EmailRequest request) {
        String email = request.email();
        emailService.generateAndSendCode(email); // 예외 발생 시 GlobalExceptionHandler에서 처리됨
        return ResponseEntity.ok(new EmailResponse("인증 코드가 이메일로 전송되었습니다.", true));
    }

    // 인증 코드 확인
    @PostMapping("/verify")
    public ResponseEntity<EmailResponse> verifyCode(@RequestBody EmailRequest request) {
        return emailService.verifyCode(request);
    }
}


