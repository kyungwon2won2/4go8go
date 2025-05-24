package com.example.demo.common.oauth.model;

import java.util.Map;

/**
 * 소셜 로그인 공통 인터페이스
 * 다양한 소셜 로그인 제공자의 사용자 정보를 표준화
 */
public interface OAuth2UserInfo {
    /**
     * 원본 속성 정보 반환
     */
    Map<String, Object> getAttributes();
    
    /**
     * 소셜 서비스의 고유 ID 반환
     */
    String getId();
    
    /**
     * 사용자 이름 반환
     */
    String getName();
    
    /**
     * 사용자 이메일 반환
     */
    String getEmail();
    
    /**
     * 사용자 프로필 이미지 URL 반환
     */
    String getImageUrl();
}