package com.example.demo.common.oauth.model;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.util.Map;

/**
 * OAuth2 인증 결과를 담는 클래스
 * 세션에 저장되므로 Serializable 구현
 */
@Getter
public class OAuthAttributes implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String name;
    private String email;
    private OAuth2UserInfo oauth2UserInfo;

    @Builder
    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey,
                           String name, String email, OAuth2UserInfo oauth2UserInfo) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.name = name;
        this.email = email;
        this.oauth2UserInfo = oauth2UserInfo;
    }

    /**
     * 소셜 로그인 제공자별 OAuthAttributes 객체 생성
     */
    public static OAuthAttributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        if ("naver".equals(registrationId)) {
            return ofNaver(userNameAttributeName, attributes);
        }
        return ofGoogle(userNameAttributeName, attributes);
    }

    /**
     * Google 로그인 정보로 OAuthAttributes 생성
     */
    private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(attributes);

        return OAuthAttributes.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .oauth2UserInfo(userInfo)
                .build();
    }

    /**
     * Naver 로그인 정보로 OAuthAttributes 생성
     */
    private static OAuthAttributes ofNaver(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        if (response == null) {
            response = attributes;  // 이미 response가 attributes인 경우 처리
        }

        NaverOAuth2UserInfo userInfo = new NaverOAuth2UserInfo(response);

        return OAuthAttributes.builder()
                .name(userInfo.getName())
                .email(userInfo.getEmail())
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .oauth2UserInfo(userInfo)
                .build();
    }
}