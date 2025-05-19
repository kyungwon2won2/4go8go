package com.example.demo.common.oauth;

import com.example.demo.domain.user.model.CustomerUser;
import com.example.demo.domain.user.model.UserRole;
import com.example.demo.domain.user.model.Users;
import com.example.demo.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserMapper userMapper;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        log.info("registrationId: {}", registrationId);
        log.info("userNameAttributeName: {}", userNameAttributeName);

        if ("naver".equals(registrationId)) {
            Map<String, Object> attributes = oAuth2User.getAttributes();
            log.info("네이버 응답 데이터 확인: {}", attributes.get("response"));
        }

        OAuthAttributes oAuthAttributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        // 현재 요청 컨텍스트에서 HttpServletRequest 가져오기
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        // 신규 사용자인지 확인
        String email = oAuthAttributes.getEmail();
        Users existingUser = userMapper.getUserByEmail(email);

        // 신규 사용자면 추가 정보 입력 필요
        if (existingUser == null) {
            // 세션에 OAuth2 정보 저장
            request.getSession().setAttribute("oauthAttributes", oAuthAttributes);
            request.getSession().setAttribute("registrationId", registrationId);
            request.getSession().setAttribute("requireAdditionalInfo", true);

            // 리다이렉트를 위한 예외 발생
            throw new OAuth2AuthenticationException("추가 정보 입력 필요");
        }

        return new CustomerUser(existingUser, oAuthAttributes.getAttributes());
    }
}