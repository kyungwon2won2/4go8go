package com.example.demo.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//정적 리소스 관리
@Configuration
public class WebConfig implements WebMvcConfigurer {
    

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Chrome DevTools 요청 처리
        registry.addResourceHandler("/.well-known/**")
                .addResourceLocations("classpath:/static/.well-known/");
        
        // 정적 리소스 경로 명시적 설정
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");
        
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");
        
        registry.addResourceHandler("/image/**")
                .addResourceLocations("classpath:/static/image/");
                
        registry.addResourceHandler("/summernote/**")
                .addResourceLocations("classpath:/static/summernote/");
    }

    
    //비동기 요청 설정
    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        // 비동기 요청 타임아웃 설정 (5분) - sse와의 충돌 방지
        configurer.setDefaultTimeout(300000);

    }

}