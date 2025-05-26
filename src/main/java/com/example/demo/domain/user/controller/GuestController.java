package com.example.demo.domain.user.controller;

import com.example.demo.common.oauth.model.OAuthAttributes;
import com.example.demo.common.oauth.service.CustomOAuth2UserService;
import com.example.demo.domain.user.dto.CreateUserDTO;
import com.example.demo.domain.user.service.GuestService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping
public class GuestController {

    private final GuestService guestService;
    private final CustomOAuth2UserService customOAuth2UserService;

    @GetMapping("/login")
    public String login(HttpServletRequest request) {
        // 세션에서 오류 메시지 초기화 (이전 로그인 시도의 오류 메시지가 남아있지 않게)
        HttpSession session = request.getSession(false);
        if (session != null) {
            // 탈퇴 계정 플래그가 있으면 유지, 나머지는 초기화
            Object deletedAccount = session.getAttribute("deletedAccount");
            Object loginError = session.getAttribute("loginError");
            
            // 세션 속성 초기화
            session.removeAttribute("requireAdditionalInfo");
            
            // 탈퇴 계정 관련 속성은 한 번 더 사용할 수 있도록 유지
            if (deletedAccount != null) {
                // 다음 요청에서는 제거되도록 플래그 설정
                session.setAttribute("clearDeletedFlag", true);
            } else if (session.getAttribute("clearDeletedFlag") != null) {
                // 이전에 표시했으면 이제 제거
                session.removeAttribute("deletedAccount");
                session.removeAttribute("loginError");
                session.removeAttribute("clearDeletedFlag");
            }
        }
        
        return "login";
    }

    @GetMapping("/join")
    public String join(@RequestParam(required = false) String social, 
                       @RequestParam(required = false) String provider,
                       HttpSession session, Model model) {
        
        // 소셜 로그인에서 온 경우 처리
        if ("true".equals(social)) {
            OAuthAttributes attributes = (OAuthAttributes) session.getAttribute("oauthAttributes");
            String registrationId = (String) session.getAttribute("registrationId");
            
            if (attributes == null || registrationId == null) {
                log.warn("소셜 로그인 정보가 세션에 없습니다. 로그인 페이지로 리다이렉트합니다.");
                return "redirect:/login";
            }
            
            // 소셜 로그인 모드 설정
            model.addAttribute("socialMode", true);
            model.addAttribute("provider", registrationId);
            
            // 소셜에서 받은 정보 미리 채우기
            model.addAttribute("socialEmail", attributes.getEmail());
            model.addAttribute("socialName", attributes.getName());
            
            // 네이버 로그인 시 추가 정보 처리
            if ("naver".equals(registrationId)) {
                Map<String, Object> attr = attributes.getAttributes();
                Map<String, Object> response = (Map<String, Object>) attr.get("response");
                if (response != null) {
                    // 생년월일 정보 추출
                    if (response.containsKey("birthyear") && response.containsKey("birthday")) {
                        String birthYear = (String) response.get("birthyear");
                        String birthday = (String) response.get("birthday");
                        String formattedBirthDate = birthYear + "-" + birthday.replace("-", "-");
                        model.addAttribute("socialBirthDate", formattedBirthDate);
                    }
                    
                    // 휴대폰 정보 추출
                    if (response.containsKey("mobile")) {
                        String mobile = (String) response.get("mobile");
                        model.addAttribute("socialPhone", mobile);
                    }
                }
            }
            
            log.info("소셜 로그인 통합 회원가입 페이지 진입: provider={}, email={}", registrationId, attributes.getEmail());
        } else {
            // 일반 회원가입 모드
            model.addAttribute("socialMode", false);
        }
        
        return "join";
    }

    @PostMapping("/join")
    public String CreateUser(@ModelAttribute CreateUserDTO dto, 
                            HttpSession session,
                            RedirectAttributes redirectAttributes) throws Exception {
        try {
            // 소셜 로그인에서 온 경우인지 확인
            OAuthAttributes attributes = (OAuthAttributes) session.getAttribute("oauthAttributes");
            String registrationId = (String) session.getAttribute("registrationId");
            
            if (attributes != null && registrationId != null) {
                // 소셜 로그인 회원가입 처리
                return processSocialSignup(dto, attributes, registrationId, session, redirectAttributes);
            } else {
                // 일반 회원가입 처리
                guestService.createUser(dto);
                redirectAttributes.addFlashAttribute("registrationSuccess", true);
                return "redirect:/";
            }
        } catch (Exception e) {
            log.error("회원가입 처리 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("registrationError", e.getMessage());
            return "redirect:/join";
        }
    }
    
    /**
     * 소셜 로그인 회원가입 처리
     */
    private String processSocialSignup(CreateUserDTO dto, OAuthAttributes attributes, 
                                     String registrationId, HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        try {
            // 생년월일 변환
            Date birthDateObj = null;
            if (dto.birthDate() != null) {
                birthDateObj = Date.from(dto.birthDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
            }
            
            // 소셜 사용자 생성 및 저장
            customOAuth2UserService.saveNewOAuthUser(
                    attributes,
                    dto.phone(),
                    dto.nickname(),
                    dto.address(),
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
            redirectAttributes.addFlashAttribute("registrationError", "소셜 로그인 회원가입 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/join?social=true";
        }
    }
}