package com.example.demo.domain.user.controller;

import com.example.demo.domain.post.dto.ProductListDto;
import com.example.demo.domain.post.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class HomeController {

	private final ProductService productService;

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
		
		// 메인 페이지용 상품 데이터 추가
		// 조회수가 높은 상품 4개 (이 상품 어때요?)
		List<ProductListDto> topViewedProducts = productService.getTopViewedProducts();
		model.addAttribute("topViewedProducts", topViewedProducts);

		// 가격이 저렴한 상품 4개 (놓치면 후회할 가격)
		List<ProductListDto> cheapestProducts = productService.getCheapestProducts();
		model.addAttribute("cheapestProducts", cheapestProducts);
		
		return "index";
	}
}
