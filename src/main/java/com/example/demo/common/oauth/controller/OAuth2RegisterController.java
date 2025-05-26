package com.example.demo.common.oauth.controller;

import com.example.demo.common.oauth.model.OAuthAttributes;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * OAuth2 추가 정보 등록 컨트롤러
 * 소셜 로그인 사용자를 통합 회원가입 페이지로 리다이렉트
 */
@Slf4j
@Controller
@RequestMapping("/oauth2/signup")
@RequiredArgsConstructor
public class OAuth2RegisterController {

    /**
     * 추가 정보 입력 폼 페이지 - 통합 회원가입 페이지로 리다이렉트
     */
    @GetMapping("/additional-info")
    public String additionalInfoForm(HttpSession session) {
        // 세션에서 OAuth2 사용자 정보 확인
        OAuthAttributes attributes = (OAuthAttributes) session.getAttribute("oauthAttributes");
        String registrationId = (String) session.getAttribute("registrationId");

        if (attributes == null || registrationId == null) {
            log.warn("소셜 로그인 정보가 세션에 없습니다. 로그인 페이지로 리다이렉트합니다.");
            return "redirect:/login";
        }

        log.info("소셜 로그인 사용자를 통합 회원가입 페이지로 리다이렉트: provider={}, email={}", 
                registrationId, attributes.getEmail());
        
        // 통합된 회원가입 페이지로 리다이렉트
        return "redirect:/join?social=true&provider=" + registrationId;
    }
}