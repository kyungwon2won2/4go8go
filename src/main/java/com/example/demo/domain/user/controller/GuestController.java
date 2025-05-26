package com.example.demo.domain.user.controller;

import com.example.demo.domain.user.dto.CreateUserDTO;
import com.example.demo.domain.user.service.GuestService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping
public class GuestController {

    private final GuestService guestService;

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
    public String join() {
        return "join";
    }

    @PostMapping("/join")
    public String CreateUser(@ModelAttribute CreateUserDTO dto, 
                            RedirectAttributes redirectAttributes) throws Exception {
        try {
            // 일반 회원가입 처리
            guestService.createUser(dto);
            redirectAttributes.addFlashAttribute("registrationSuccess", true);
            return "redirect:/";
        } catch (Exception e) {
            log.error("회원가입 처리 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("registrationError", e.getMessage());
            return "redirect:/join";
        }
    }
}