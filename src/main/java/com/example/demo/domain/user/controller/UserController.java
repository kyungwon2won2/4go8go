package com.example.demo.domain.user.controller;

import com.example.demo.domain.user.dto.UpdateUserDTO;
import com.example.demo.domain.user.model.CustomerUser;
import com.example.demo.domain.user.model.Users;
import com.example.demo.domain.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	// 사용자 정보 조회
	@GetMapping("/profile")
	public String profile(Model model, Principal principal) {
		// 변수 미리 선언
		String email = null;
		String name = null;
		Users user = null;

		if (principal != null) {
			// Principal 객체 타입에 따른 처리
			if (principal instanceof UsernamePasswordAuthenticationToken) {
				UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
				Object principalObj = token.getPrincipal();

				if (principalObj instanceof CustomerUser) {
					CustomerUser customerUser = (CustomerUser) principalObj;
					email = customerUser.getUsername();
					user = userService.getUserByEmail(email);
					name = user.getName();
				}
			} else if (principal instanceof OAuth2AuthenticationToken) {
				OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) principal;
				OAuth2User oauth2User = token.getPrincipal();
				Map<String, Object> attributes = oauth2User.getAttributes();

				// 소셜 로그인 유형에 따라 이름 정보 가져오기
				if ("google".equals(token.getAuthorizedClientRegistrationId())) {
					email = (String) attributes.get("email");
					name = (String) attributes.get("name");
				} else if ("naver".equals(token.getAuthorizedClientRegistrationId())) {
					Map<String, Object> response = (Map<String, Object>) attributes.get("response");
					if (response != null) {
						email = (String) response.get("email");
						name = (String) response.get("name");
					}
				} else if ("kakao".equals(token.getAuthorizedClientRegistrationId())) {
					Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
					Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");

					if (kakaoAccount != null) {
						email = (String) kakaoAccount.get("email");
					}
					if (properties != null) {
						name = (String) properties.get("nickname");
					}
				}

				// 이메일로 사용자 정보 조회
				if (email != null) {
					user = userService.getUserByEmail(email);
				}
			}

			// 모델에 데이터 추가
			model.addAttribute("email", email);
			model.addAttribute("name", name);
			model.addAttribute("user", user);
		}

		return "user/profile";
	}

	// 사용자 목록 조회 (관리자만 접근 가능)
	@GetMapping("/list")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public String userList(Model model) {
		model.addAttribute("users", userService.getAllUsers());
		return "user/list";
	}

	@GetMapping("/edit")
	public String editUserForm(Model model, Principal principal) {
		Users user = userService.getUserByEmail(principal.getName());
		model.addAttribute("user", user);
		return "user/edit";
	}

	@PostMapping("/edit")
	public String updateUser(@Valid UpdateUserDTO dto) {
		userService.updateUser(dto);
		return "redirect:/user/profile";
	}

	// 회원 탈퇴
	@PostMapping("/delete")
	public String deleteUser(Principal principal, HttpServletRequest request, HttpServletResponse response) {
		// 사용자 탈퇴 처리 (상태를 DELETED로 변경)
		userService.deleteUser(principal.getName());
		
		// 로그아웃 처리 - 세션 무효화
		HttpSession session = request.getSession(false);
		if (session != null) {
			// 세션 무효화 (모든 세션 데이터 삭제)
			session.invalidate();
		}
		
		// JSESSIONID 쿠키 삭제
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if ("JSESSIONID".equals(cookie.getName())) {
					cookie.setMaxAge(0);
					cookie.setPath("/");
					response.addCookie(cookie);
					break;
				}
			}
		}
		
		// Spring Security 컨텍스트 클리어
		SecurityContextHolder.clearContext();
		
		// 로그아웃 메시지 표시를 위한 리다이렉트 파라미터 추가
		return "redirect:/?withdrawn=true";
	}
}