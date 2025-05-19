package com.example.demo.common.oauth;

import com.example.demo.domain.user.model.CustomerUser;
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
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
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
        Users user = saveOrUpdate(oAuthAttributes, registrationId);

        return new CustomerUser(
                user,
                oAuthAttributes.getAttributes()
        );
    }

    private Users saveOrUpdate(OAuthAttributes attributes, String registrationId) {
        String email = attributes.getEmail();
        Users existingUser = userMapper.getUserByEmail(email);

        if (existingUser == null) {
            // 새 사용자 등록
            Users newUser = new Users();
            newUser.setEmail(email);
            newUser.setName(attributes.getName());
            newUser.setNickname(attributes.getName());
            newUser.setEmailVerified(true);
            newUser.setSocialType(registrationId);
            newUser.setSocialId(attributes.getOauth2UserInfo().getId());

            UserRole role = new UserRole();
            role.setRoleName("ROLE_USER");

            List<UserRole> roleList = new ArrayList<>();
            roleList.add(role);
            newUser.setRoleList(roleList);

            try {
                userMapper.join(newUser);
                UserRole userRole = new UserRole();
                userRole.setUserId(newUser.getUserId());
                userRole.setRoleName("ROLE_USER");
                userMapper.insertAuth(userRole);
                return userMapper.getUserByEmail(email);
            } catch (Exception e) {
                log.error("소셜 사용자 등록 중 오류 발생", e);
                throw new RuntimeException("소셜 사용자 등록 중 오류 발생", e);
            }
        }

        return existingUser;
    }
}