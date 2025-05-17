package com.example.demo.common.oauth;

import lombok.Builder;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class OAuthAttributes {
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

    public static OAuthAttributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        if ("kakao".equals(registrationId)) {
            return ofKakao(userNameAttributeName, attributes);
        } else if ("naver".equals(registrationId)) {
            return ofNaver(userNameAttributeName, attributes);
        }
        return ofGoogle(userNameAttributeName, attributes);
    }

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

    private static OAuthAttributes ofKakao(String userNameAttributeName, Map<String, Object> attributes) {
        KakaoOAuth2UserInfo userInfo = new KakaoOAuth2UserInfo(attributes);

        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> kakaoProfile = (Map<String, Object>) kakaoAccount.get("profile");

        return OAuthAttributes.builder()
                .name((String) kakaoProfile.get("nickname"))
                .email((String) kakaoAccount.get("email"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .oauth2UserInfo(userInfo)
                .build();
    }

    private static OAuthAttributes ofNaver(String userNameAttributeName, Map<String, Object> attributes) {
        // attributes가 response 키를 포함하는지 확인
        Map<String, Object> response;
        if (attributes.containsKey("response")) {
            response = (Map<String, Object>) attributes.get("response");
        } else {
            // 이미 response 자체가 전달된 경우
            response = attributes;
        }

        NaverOAuth2UserInfo userInfo = new NaverOAuth2UserInfo(response);

        return OAuthAttributes.builder()
                .name((String) response.get("name"))
                .email((String) response.get("email"))
                .attributes(attributes) // 원래 attributes를 유지
                .nameAttributeKey(userNameAttributeName)
                .oauth2UserInfo(userInfo)
                .build();
    }
}