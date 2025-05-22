package com.example.demo.common.oauth;

import com.example.demo.domain.user.model.CustomerUser;
import com.example.demo.domain.user.model.UserRole;
import com.example.demo.domain.user.model.Users;
import com.example.demo.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserMapper userMapper;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        try {
            return processOAuth2User(userRequest, oauth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
        // OAuth2 로그인 플랫폼(google, naver, kakao) 구분
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oauth2User.getAttributes());

        Users user = saveOrUpdate(attributes);

        // 탈퇴한 회원인지 확인 - SecurityConfig에서 설정한 failureHandler로 예외 전달
        if ("DELETED".equals(user.getStatus())) {
            throw new OAuth2AuthenticationException("탈퇴한 회원입니다.");
        }

        return new CustomerUser(user, oauth2User.getAttributes());
    }

    private Users saveOrUpdate(OAuthAttributes attributes) {
        Users user = userMapper.getUserByEmail(attributes.getEmail());

        if (user == null) {
            // 회원 정보가 없으면 새로 생성
            user = Users.builder()
                    .email(attributes.getEmail())
                    .name(attributes.getName())
                    .socialType(getSocialType(attributes.getOauth2UserInfo()))
                    .socialId(attributes.getOauth2UserInfo().getId())
                    .nickname(attributes.getName()) // 기본적으로 이름을 닉네임으로 설정
                    .status("ACTIVE") // 상태를 ACTIVE로 설정
                    .build();

            // 회원 가입
            try {
                userMapper.join(user);

                // 권한 추가
                UserRole role = new UserRole();
                role.setUserId(user.getUserId());
                role.setRoleName("ROLE_USER");
                userMapper.insertAuth(role);

                // 권한 목록 생성
                List<UserRole> roleList = new ArrayList<>();
                roleList.add(role);
                user.setRoleList(roleList);

            } catch (Exception e) {
                log.error("OAuth2 회원가입 실패: {}", e.getMessage());
            }
        }

        return user;
    }

    private String getSocialType(OAuth2UserInfo userInfo) {
        if (userInfo instanceof GoogleOAuth2UserInfo) {
            return "GOOGLE";
        } else if (userInfo instanceof NaverOAuth2UserInfo) {
            return "NAVER";
        } else if (userInfo instanceof KakaoOAuth2UserInfo) {
            return "KAKAO";
        }
        return "UNKNOWN";
    }
}