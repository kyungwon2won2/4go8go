package com.example.demo.common.oauth.controller;

import com.example.demo.common.oauth.model.OAuthAttributes;
import com.example.demo.common.oauth.service.CustomOAuth2UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * OAuth2 추가 정보 등록 컨트롤러
 * 소셜 로그인 사용자의 추가 정보 입력 처리
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
        // 세션에서 OAuth2 사용자 정보 확인
        OAuthAttributes attributes = (OAuthAttributes) session.getAttribute("oauthAttributes");
        String registrationId = (String) session.getAttribute("registrationId");

        if (attributes == null || registrationId == null) {
            log.warn("소셜 로그인 정보가 세션에 없습니다. 로그인 페이지로 리다이렉트합니다.");
            return "redirect:/login";
        }

        log.info("소셜 로그인 추가 정보 입력 페이지 진입: provider={}, email={}", 
                registrationId, attributes.getEmail());
        
        // 모델에 소셜 로그인 정보 추가
        model.addAttribute("provider", registrationId);
        model.addAttribute("email", attributes.getEmail());
        model.addAttribute("name", attributes.getName());
        
        // 추가 정보 입력 페이지로 이동
        return "oauth2/additional-info";
    }

    /**
     * 추가 정보 처리
     */
    @PostMapping("/additional-info")
    public String processAdditionalInfo(@RequestParam String phone,
                                      @RequestParam String nickname,
                                      @RequestParam(required = false) String address,
                                      @RequestParam(required = false) String birthDate,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        try {
            // 세션에서 OAuth2 사용자 정보 확인
            OAuthAttributes attributes = (OAuthAttributes) session.getAttribute("oauthAttributes");
            String registrationId = (String) session.getAttribute("registrationId");

            if (attributes == null || registrationId == null) {
                log.warn("소셜 로그인 정보가 세션에 없습니다.");
                redirectAttributes.addFlashAttribute("error", "세션이 만료되었습니다. 다시 로그인해주세요.");
                return "redirect:/login";
            }

            // 생년월일 처리
            Date birthDateObj = null;
            if (birthDate != null && !birthDate.isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    birthDateObj = sdf.parse(birthDate);
                } catch (ParseException e) {
                    log.warn("생년월일 파싱 실패: {}", birthDate);
                }
            }

            // 소셜 사용자 저장
            customOAuth2UserService.saveNewOAuthUser(
                    attributes,
                    phone,
                    nickname,
                    address,
                    birthDateObj,
                    registrationId
            );

            // 세션 정리
            session.removeAttribute("oauthAttributes");
            session.removeAttribute("registrationId");
            session.removeAttribute("requireAdditionalInfo");

            redirectAttributes.addFlashAttribute("registrationSuccess", true);
            redirectAttributes.addFlashAttribute("socialRegistration", true);

            log.info("소셜 로그인 회원가입 완료: provider={}, email={}", registrationId, attributes.getEmail());
            return "redirect:/";

        } catch (Exception e) {
            log.error("소셜 로그인 회원가입 처리 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("error", "회원가입 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/oauth2/signup/additional-info";
        }
    }
}