package com.example.demo.domain.user.controller;



import com.example.demo.domain.user.dto.UpdateUserDTO;
import com.example.demo.domain.user.model.Users;
import com.example.demo.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
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
	public String userProfile(Model model, Principal principal) {
		log.info("[[[[  /user ]]]]");

		String email = null;

		// OAuth2 소셜 로그인 사용자인 경우
		if (principal instanceof OAuth2AuthenticationToken) {
			OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) principal;
			Map<String, Object> attributes = token.getPrincipal().getAttributes();

			// 소셜 로그인 유형에 따라 이메일 정보 가져오기
			if ("google".equals(token.getAuthorizedClientRegistrationId())) {
				email = (String) attributes.get("email");
			} else if ("naver".equals(token.getAuthorizedClientRegistrationId())) {
				Map<String, Object> response = (Map<String, Object>) attributes.get("response");
				if (response != null) {
					email = (String) response.get("email");
				}
			} else if ("kakao".equals(token.getAuthorizedClientRegistrationId())) {
				Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
				if (kakaoAccount != null) {
					email = (String) kakaoAccount.get("email");
				}
			}
		} else {
			// 일반 로그인 사용자인 경우
			email = principal.getName();
		}

		// 이메일로 사용자 조회
		Users user = userService.getUserByEmail(email);
		model.addAttribute("user", user);
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
		return "redirect:/user";
	}

	// 회원 탈퇴
	@PostMapping("/delete")
	public String deleteUser(Principal principal) {
		userService.deleteUser(principal.getName());
		return "redirect:/";
	}
}