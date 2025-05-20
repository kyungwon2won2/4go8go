package com.example.demo.common.email.controller;

import com.example.demo.common.email.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    // 인증 코드 요청
    @PostMapping("/send")
    public ResponseEntity<?> sendVerificationCode(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        emailService.generateAndSendCode(email); // 예외 발생 시 GlobalExceptionHandler에서 처리됨
        return ResponseEntity.ok("인증 코드가 이메일로 전송되었습니다.");
    }

    // 인증 코드 확인
    @PostMapping("/verify")
    public ResponseEntity<?> verifyCode(@RequestParam String email,
                                        @RequestParam String code) {
        boolean isValid = emailService.verifyCode(email, code);
        if (isValid) {
            return ResponseEntity.ok("이메일 인증 성공");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 코드가 유효하지 않거나 만료되었습니다.");
        }
    }
}


