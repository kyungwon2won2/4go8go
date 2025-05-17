package com.example.demo.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 웹 설정 클래스
 * 정적 리소스와 관련된 설정을 처리
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    /**
     * Chrome DevTools 요청을 처리하기 위한 리소스 핸들러 설정
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Chrome DevTools 요청 처리
        registry.addResourceHandler("/.well-known/**")
                .addResourceLocations("classpath:/static/.well-known/");
    }
} 