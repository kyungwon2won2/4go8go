package com.example.demo.domain.user.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class EmailVerification {
    private int id;
    private String email;
    private String token;
    private boolean verified;
    private Date createdAt;
    private Date expiresAt;
    private Date verifiedAt;
    
    // 생성자 추가
    public EmailVerification(String email, String token) {
        this.email = email;
        this.token = token;
        this.verified = false;
        this.createdAt = new Date();
        // 기본적으로 24시간 후 만료
        this.expiresAt = new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000);
    }
    
    public boolean isExpired() {
        return new Date().after(expiresAt);
    }
}