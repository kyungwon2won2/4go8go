package com.example.demo.domain.user.controller;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.Map;

@Controller
public class HomeController {

	@GetMapping({"","/"})
	public String home(Model model, Principal principal) {
		boolean isLoggedIn = principal != null;
		String loginId = "guest";
		String userName = "손님";

		if (isLoggedIn) {
			if (principal instanceof OAuth2AuthenticationToken) {
				OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) principal;
				Map<String, Object> attributes = token.getPrincipal().getAttributes();

				// 소셜 로그인 유형에 따라 이름 정보 가져오기
				if ("google".equals(token.getAuthorizedClientRegistrationId())) {
					loginId = (String) attributes.get("email");
					userName = (String) attributes.get("name");
				} else if ("naver".equals(token.getAuthorizedClientRegistrationId())) {
					Map<String, Object> response = (Map<String, Object>) attributes.get("response");
					if (response != null) {
						loginId = (String) response.get("email");
						userName = (String) response.get("name");
					}
				} else if ("kakao".equals(token.getAuthorizedClientRegistrationId())) {
					loginId = String.valueOf(attributes.get("id"));
					Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
					if (properties != null) {
						userName = (String) properties.get("nickname");
					}
				}
			} else {
				loginId = principal.getName();
				userName = loginId; // 일반 로그인인 경우 이름은 이메일로 설정
			}
		}

		model.addAttribute("loginId", loginId);
		model.addAttribute("userName", userName);
		model.addAttribute("isLoggedIn", isLoggedIn);
		return "index";
	}
}
