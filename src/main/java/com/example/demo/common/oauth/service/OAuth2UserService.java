package com.example.demo.common.oauth.service;

import com.example.demo.common.oauth.model.OAuthAttributes;
import com.example.demo.domain.user.model.Users;
import java.util.Date;

/**
 * OAuth2 사용자 관련 서비스 인터페이스
 */
public interface OAuth2UserService {

    /**
     * 소셜 로그인 사용자 저장
     *
     * @param attributes    OAuth 속성 정보
     * @param phone         전화번호
     * @param nickname      닉네임
     * @param address       주소
     * @param birthDate     생일
     * @param registrationId 소셜 로그인 제공자 ID
     * @return 생성된 사용자 정보
     */
    Users saveNewOAuthUser(OAuthAttributes attributes, String phone, String nickname,
                          String address, Date birthDate, String registrationId);
    
    /**
     * 이메일로 소셜 사용자 조회
     * 
     * @param email 이메일
     * @return 사용자 정보
     */
    Users getOAuth2UserByEmail(String email);
    
    /**
     * 소셜 사용자 필수 정보 확인
     * 
     * @param user 사용자 정보
     * @return 필수 정보가 모두 존재하면 true, 아니면 false
     */
    boolean hasRequiredInfo(Users user);
}