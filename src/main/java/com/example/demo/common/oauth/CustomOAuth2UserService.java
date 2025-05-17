package com.example.demo.common.oauth;

import com.example.demo.domain.user.model.UserRole;
import com.example.demo.domain.user.model.Users;
import com.example.demo.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserMapper userMapper;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.info("OAuth 로그인 진행 중: {}", registrationId);

        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
        log.info("userNameAttributeName: {}", userNameAttributeName);

        Map<String, Object> attributes = oAuth2User.getAttributes();
        log.info("OAuth2 속성: {}", attributes);

        // 네이버 로그인이면서 응답이 없는 경우
        if ("naver".equals(registrationId)) {
            log.info("네이버 로그인 처리");
            if (attributes.containsKey("response")) {
                log.info("네이버 응답 데이터 확인: {}", attributes.get("response"));
            } else {
                log.warn("네이버 응답에 'response' 키가 없습니다!");
            }
        }

        // OAuthAttributes 객체 생성
        OAuthAttributes oAuthAttributes =
                OAuthAttributes.of(registrationId, userNameAttributeName, attributes);

        // OAuthAttributes를 사용하여 사용자 저장 또는 업데이트
        Users user = saveOrUpdate(oAuthAttributes, registrationId);

        // DefaultOAuth2User 객체 생성 시 oAuthAttributes의 정보 사용
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRoleList().get(0).getRoleName())),
                oAuthAttributes.getAttributes(),
                oAuthAttributes.getNameAttributeKey());
    }

    private Users saveOrUpdate(OAuthAttributes attributes, String socialType) {
        Users userEntity = userMapper.getUserByEmail(attributes.getEmail());

        if(userEntity == null) {
            // 새 사용자 등록
            Users user = new Users();
            user.setEmail(attributes.getEmail());
            user.setName(attributes.getName());
            user.setNickname(attributes.getName()); // 기본값으로 이름 사용
            user.setPhone("소셜로그인사용자");  // 기본 전화번호 설정
            user.setPassword(""); // OAuth 로그인은 비밀번호가 필요 없음
            user.setSocialType(socialType);
            user.setSocialId(attributes.getOauth2UserInfo().getId());
            user.setEmailVerified(true);  // 소셜 로그인은 이메일 인증 필요 없음
            user.setCreatedAt(new Date());
            user.setUpdatedAt(new Date());

            try {
                userMapper.join(user);

                // 권한 추가
                UserRole role = new UserRole();
                role.setUserId(user.getUserId());
                role.setRoleName("ROLE_USER");
                userMapper.insertAuth(role);

                // 사용자 정보 다시 조회 (roleList 포함)
                return userMapper.getUserByEmail(user.getEmail());
            } catch (Exception e) {
                log.error("OAuth 사용자 저장 중 오류 발생", e);
                throw new RuntimeException("OAuth 사용자 저장 중 오류 발생", e);
            }
        }

        // 기존 사용자는 소셜 정보 업데이트
        userEntity.setSocialType(socialType);
        userEntity.setSocialId(attributes.getOauth2UserInfo().getId());
        userEntity.setUpdatedAt(new Date());
        userMapper.updateUser(userEntity);

        return userEntity;
    }
}