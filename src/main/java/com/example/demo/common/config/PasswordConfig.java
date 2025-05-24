package com.example.demo.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 비밀번호 인코딩 설정
 * SecurityConfig에서 분리하여 순환 참조 문제 해결
 */
@Configuration
public class PasswordConfig {

    /**
     * 비밀번호 인코더 빈 등록
     * BCrypt 알고리즘 사용
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
