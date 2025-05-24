package com.example.demo.common.oauth.controller;

import com.example.demo.common.oauth.model.OAuthAttributes;
import com.example.demo.common.oauth.service.CustomOAuth2UserService;
import com.example.demo.domain.user.model.Users;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/**
 * OAuth2 추가 정보 등록 컨트롤러
 * 소셜 로그인 사용자의 추가 정보 입력을 처리
 */
@Slf4j
@Controller
@RequestMapping("/oauth2/signup")
@RequiredArgsConstructor
public class OAuth2RegisterController {

    private final CustomOAuth2UserService customOAuth2UserService;

    /**
     * 추가 정보 입력 폼 페이지
     */
    @GetMapping("/additional-info")
    public String additionalInfoForm(HttpSession session, Model model) {
        // 세션에서 OAuth2 사용자 정보 가져오기
        OAuthAttributes attributes = (OAuthAttributes) session.getAttribute("oauthAttributes");

        if (attributes == null) {
            return "redirect:/login";
        }

        // 모델에 OAuth2 사용자 정보 추가
        model.addAttribute("email", attributes.getEmail());
        model.addAttribute("name", attributes.getName());

        // 생년월일 정보 확인 및 추가 (있는 경우)
        Map<String, Object> attr = attributes.getAttributes();
        String registrationId = (String) session.getAttribute("registrationId");

        // 네이버 로그인 시 생년월일 정보 추출
        if ("naver".equals(registrationId)) {
            Map<String, Object> response = (Map<String, Object>) attr.get("response");
            if (response != null && response.containsKey("birthyear") && response.containsKey("birthday")) {
                String birthYear = (String) response.get("birthyear");
                String birthday = (String) response.get("birthday");
                // 예: 1990-01-01 형태로 변환
                model.addAttribute("birthDate", birthYear + "-" + birthday.replace(".", "-"));
            }
        }

        return "oauth2/additional-info";
    }

    /**
     * 추가 정보 제출 처리
     */
    @PostMapping("/additional-info")
    public String processAdditionalInfo(@RequestParam String phone,
                                        @RequestParam(required = false) String nickname,
                                        @RequestParam(required = false) String address,
                                        @RequestParam String birthDate,
                                        HttpSession session) {
        // 세션에서 OAuth2 사용자 정보 가져오기
        OAuthAttributes attributes = (OAuthAttributes) session.getAttribute("oauthAttributes");
        String registrationId = (String) session.getAttribute("registrationId");

        if (attributes == null || registrationId == null) {
            return "redirect:/login";
        }

        try {
            // 생년월일 변환
            Date birthDateObj = null;
            if (birthDate != null && !birthDate.isEmpty()) {
                LocalDate localDate = LocalDate.parse(birthDate);
                birthDateObj = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            }

            // 새 OAuth2 사용자 생성 및 저장
            Users newUser = customOAuth2UserService.saveNewOAuthUser(
                    attributes, 
                    phone, 
                    nickname, 
                    address, 
                    birthDateObj, 
                    registrationId
            );

            // 세션에서 임시 데이터 제거
            session.removeAttribute("oauthAttributes");
            session.removeAttribute("registrationId");
            session.removeAttribute("requireAdditionalInfo");

            return "redirect:/login?registered=true";
        } catch (Exception e) {
            log.error("소셜 사용자 등록 중 오류 발생", e);
            return "redirect:/login?error=registration";
        }
    }
}