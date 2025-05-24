package com.example.demo.common.oauth.service;

import com.example.demo.common.oauth.model.GoogleOAuth2UserInfo;
import com.example.demo.common.oauth.model.NaverOAuth2UserInfo;
import com.example.demo.common.oauth.model.OAuth2UserInfo;
import com.example.demo.common.oauth.model.OAuthAttributes;
import com.example.demo.domain.user.model.CustomerUser;
import com.example.demo.domain.user.model.UserRole;
import com.example.demo.domain.user.model.Users;
import com.example.demo.mapper.UserMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * OAuth2 인증 서비스
 * Spring Security의 OAuth2UserService 구현체
 * OAuth2UserService 인터페이스도 구현하여 순환 참조 제거
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService implements OAuth2UserService {

    private final UserMapper userMapper;
    private final HttpSession session;
    private final PasswordEncoder passwordEncoder;

    /**
     * OAuth2 사용자 로딩
     * Spring Security에서 호출되는 메서드
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            OAuth2User oauth2User = super.loadUser(userRequest);
            log.debug("OAuth2 사용자 정보 로드 성공: {}", oauth2User);
            
            return processOAuth2User(userRequest, oauth2User);
        } catch (AuthenticationException ex) {
            log.error("OAuth2 인증 예외 발생: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("OAuth2 로딩 중 예외 발생", ex);
            throw new InternalAuthenticationServiceException("OAuth2 사용자 처리 중 오류: " + ex.getMessage(), ex);
        }
    }

    /**
     * OAuth2 사용자 처리
     * 소셜 로그인 사용자 정보 처리 및 세션 저장
     */
    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
        try {
            // OAuth2 로그인 플랫폼(google, naver) 구분
            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                    .getUserInfoEndpoint().getUserNameAttributeName();

            log.debug("OAuth2 로그인 시도 - 제공자: {}, 속성 이름: {}", registrationId, userNameAttributeName);
            log.debug("OAuth2 사용자 속성: {}", oauth2User.getAttributes());

            // OAuth2 속성 추출
            OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oauth2User.getAttributes());
            log.debug("추출된 OAuthAttributes: 이름={}, 이메일={}", attributes.getName(), attributes.getEmail());

            // 세션에 소셜 로그인 제공자 정보 저장
            session.setAttribute("registrationId", registrationId);
            session.setAttribute("oauthAttributes", attributes);
            
            if (attributes.getEmail() == null) {
                log.error("OAuth2 사용자 이메일이 없습니다. 속성: {}", attributes);
                throw new OAuth2AuthenticationException("이메일 정보를 가져올 수 없습니다.");
            }

            // 이메일로 사용자 조회
            Users user = userMapper.getUserByEmail(attributes.getEmail());
            log.debug("이메일로 사용자 조회 결과: {}", user != null ? user.getEmail() : "사용자 없음");

            // 사용자 정보가 없거나 필수 정보가 없는 경우
            if (user == null || !hasRequiredInfo(user)) {
                log.info("사용자 없거나 필수 정보 부족. 추가 정보 입력 필요: {}", attributes.getEmail());
                session.setAttribute("requireAdditionalInfo", true);
                throw new OAuth2AuthenticationException("추가 정보 입력이 필요합니다.");
            }

            // 탈퇴한 회원인지 확인
            if ("DELETED".equals(user.getStatus())) {
                log.warn("탈퇴한 회원 로그인 시도: {}", user.getEmail());
                // 세션에 직접 오류 메시지 저장
                session.setAttribute("loginError", "탈퇴한 회원입니다.");
                session.setAttribute("deletedAccount", true);
                throw new OAuth2AuthenticationException("탈퇴한 회원입니다.");
            }

            return new CustomerUser(user, oauth2User.getAttributes());
        } catch (Exception e) {
            log.error("OAuth2 사용자 처리 중 예외 발생", e);
            throw e;
        }
    }

    /**
     * 소셜 로그인 사용자 저장
     */
    @Override
    @Transactional
    public Users saveNewOAuthUser(OAuthAttributes attributes, String phone, String nickname, 
                                 String address, Date birthDate, String registrationId) {
        
        // 새 사용자 생성
        Users newUser = Users.builder()
                .email(attributes.getEmail())
                .name(attributes.getName())
                .phone(phone)
                .nickname(nickname != null ? nickname : attributes.getName())
                .address(address)
                .birthDate(birthDate)
                .socialType(registrationId.toUpperCase())
                .socialId(attributes.getOauth2UserInfo().getId())
                .status("ACTIVE")
                .receiveMail(true)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
                
        // 임시 비밀번호 설정 (소셜 로그인이므로 실제로는 사용되지 않음)
        String tempPassword = UUID.randomUUID().toString();
        String encodedPassword = passwordEncoder.encode(tempPassword);
        newUser.setPassword(encodedPassword);

        try {
            userMapper.join(newUser);

            // 권한 추가
            UserRole role = new UserRole();
            role.setUserId(newUser.getUserId());
            role.setRoleName("ROLE_USER");
            userMapper.insertAuth(role);

            // 권한 목록 생성
            List<UserRole> roleList = new ArrayList<>();
            roleList.add(role);
            newUser.setRoleList(roleList);

            return newUser;
        } catch (Exception e) {
            log.error("OAuth2 회원가입 실패: {}", e.getMessage());
            throw new RuntimeException("사용자 등록 중 오류가 발생했습니다", e);
        }
    }

    /**
     * 이메일로 소셜 사용자 조회
     */
    @Override
    public Users getOAuth2UserByEmail(String email) {
        return userMapper.getUserByEmail(email);
    }

    /**
     * 소셜 사용자 필수 정보 확인
     */
    @Override
    public boolean hasRequiredInfo(Users user) {
        return user != null 
                && user.getPhone() != null 
                && !user.getPhone().isEmpty();
    }
}