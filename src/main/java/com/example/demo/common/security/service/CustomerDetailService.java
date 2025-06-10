package com.example.demo.common.security.service;

import com.example.demo.common.oauth.model.GoogleOAuth2UserInfo;
import com.example.demo.common.oauth.model.NaverOAuth2UserInfo;
import com.example.demo.common.oauth.model.OAuth2UserInfo;
import com.example.demo.domain.user.model.CustomerUser;
import com.example.demo.domain.user.model.UserRole;
import com.example.demo.domain.user.model.Users;
import com.example.demo.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 사용자 인증 및 OAuth2 인증 서비스
 * 
 * UserDetailsService: 일반 로그인 처리
 * OAuth2UserService: 소셜 로그인 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerDetailService implements UserDetailsService, OAuth2UserService<OAuth2UserRequest, OAuth2User> {

	private final UserMapper userMapper;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Users user = userMapper.login(username);

		if (user == null) {
			throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
		}

		// 탈퇴한 회원 확인
		if (user.getStatus() != null && user.getStatus().equals("DELETED")) {
			throw new InternalAuthenticationServiceException("탈퇴한 회원입니다.");
		}

		// 정지된 회원 확인
		if (user.getStatus() != null && (user.getStatus().equals("정지") || 
			user.getStatus().equalsIgnoreCase("suspended"))) {
			throw new InternalAuthenticationServiceException("정지된 계정입니다. 관리자에게 문의하세요.");
		}

		// 권한 정보 추가 로딩
		List<UserRole> roles = userMapper.getUserRolesByUserId(user.getUserId());
		user.setRoleList(roles);

		return new CustomerUser(user);
	}

	@Override
	@Transactional
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
		OAuth2User oAuth2User = delegate.loadUser(userRequest);

		// OAuth2 서비스 ID (google, naver 등)
		String registrationId = userRequest.getClientRegistration().getRegistrationId();

		String userNameAttributeName = userRequest.getClientRegistration()
				.getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

		Map<String, Object> attributes = oAuth2User.getAttributes();

		// OAuth2UserInfo 객체 생성 (서비스별로 다른 구현체)
		OAuth2UserInfo oauth2UserInfo = getOAuth2UserInfo(registrationId, attributes);

		if (oauth2UserInfo == null) {
			throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다.");
		}

		// OAuth2 로그인 사용자 정보 (이메일, 이름, 소셜ID 등)
		String email = oauth2UserInfo.getEmail();
		String name = oauth2UserInfo.getName();
		String socialId = oauth2UserInfo.getId();

		// 이메일이 없는 경우 처리
		if (email == null || email.isEmpty()) {
			email = registrationId + "_" + socialId + "@placeholder.com";
		}

		Users user = userMapper.getUserByEmail(email);

		// 소셜 로그인 사용자가 DB에 없으면 자동 회원가입 처리
		if (user == null) {
			user = createOAuth2User(email, name, registrationId, socialId);
		}
		// 사용자가 이미 존재하는 경우 상태 확인
		else if (user.getStatus() != null && "DELETED".equals(user.getStatus())) {
			// 탈퇴한 회원인 경우 별도 처리
			throw new InternalAuthenticationServiceException("탈퇴한 회원입니다.");
		}
		// 정지된 회원 확인 (OAuth2)
		else if (user.getStatus() != null && (user.getStatus().equals("정지") || 
			user.getStatus().equalsIgnoreCase("suspended"))) {
			throw new InternalAuthenticationServiceException("정지된 계정입니다. 관리자에게 문의하세요.");
		}

		// OAuth2 로그인 시에도 권한 정보 로딩
		List<UserRole> roles = userMapper.getUserRolesByUserId(user.getUserId());
		user.setRoleList(roles);

		return new CustomerUser(user, attributes);
	}

	private OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
		if ("google".equals(registrationId)) {
			return new GoogleOAuth2UserInfo(attributes);
		} else if ("naver".equals(registrationId)) {
			Map<String, Object> response = (Map<String, Object>) attributes.get("response");
			if (response == null) {
				response = attributes;
			}
			return new NaverOAuth2UserInfo(response);
		}
		return null;
	}

	@Transactional
	private Users createOAuth2User(String email, String name, String registrationId, String socialId) {
		try {
			// 기본 회원 정보 설정
			Users user = new Users();
			user.setEmail(email);
			user.setName(name != null ? name : "사용자");
			user.setPassword(UUID.randomUUID().toString());  // 임의 비밀번호 설정
			user.setNickname(name != null ? name : "사용자_" + System.currentTimeMillis());
			user.setSocialType(registrationId.toUpperCase());
			user.setSocialId(socialId);
			user.setEmailVerified(true);
			user.setStatus("ACTIVE");

			// 회원 등록
			int result = userMapper.join(user);

			if (result > 0) {
				// 회원 권한 등록
				UserRole userRole = new UserRole();
				userRole.setUserId(user.getUserId());
				userRole.setRoleName("ROLE_USER");
				userMapper.insertAuth(userRole);

				// 저장된 회원 정보 조회
				user = userMapper.getUserByEmail(email);
				if (user == null) {
					throw new RuntimeException("사용자 생성 후 조회 실패");
				}
				
				// 새로 생성된 사용자의 권한 정보도 로딩
				List<UserRole> roles = userMapper.getUserRolesByUserId(user.getUserId());
				user.setRoleList(roles);
			} else {
				throw new RuntimeException("사용자 등록 실패");
			}

			return user;
		} catch (Exception e) {
			log.error("소셜 로그인 회원 가입 처리 중 오류: ", e);
			throw new OAuth2AuthenticationException("소셜 로그인 중 오류가 발생했습니다.");
		}
	}
}