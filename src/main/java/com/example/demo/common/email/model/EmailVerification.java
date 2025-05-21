package com.example.demo.common.email.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class EmailVerification {
    private String email;
    private String code;
    private LocalDateTime expiresAt;
    private int attempts;
    private boolean used;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean canAttempt(int maxTries) {
        return attempts < maxTries && !isUsed() && !isExpired();
    }

    public void markUsed() {
        this.used = true;
    }

    public void incrementAttempts() {
        this.attempts++;
    }

    public EmailVerification(String email, String code, LocalDateTime expiresAt) {
        this.email = email.toLowerCase(); // 대소문자 무시 처리
        this.code = code;
        this.expiresAt = expiresAt;
        this.attempts = 0;
        this.used = false;
    }
}

