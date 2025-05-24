package com.example.demo.domain.user.controller;

import com.example.demo.domain.user.model.Users;
import com.example.demo.domain.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
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

/**
 * 계정 복구 관련 컨트롤러
 */
@Slf4j
@Controller
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountRecoveryController {

    private final UserService userService;
    
    /**
     * 계정 복구 페이지 표시
     */
    @GetMapping("/recover")
    public String showRecoveryPage(@RequestParam String email, Model model, HttpServletRequest request) {
        log.info("계정 복구 페이지 요청 - 이메일: {}", email);
        
        // 요청 정보 로깅
        log.info("요청 URL: {}", request.getRequestURL());
        log.info("요청 파라미터: {}", request.getParameterMap());
        
        // 세션 정보 로깅
        HttpSession session = request.getSession(false);
        if (session != null) {
            log.info("세션 ID: {}", session.getId());
            log.info("세션 lastLoginEmail: {}", session.getAttribute("lastLoginEmail"));
            log.info("세션 oauth2Email: {}", session.getAttribute("oauth2Email"));
        }
        
        // 사용자 정보 조회
        Users user = userService.getUserByEmail(email);
        if (user != null) {
            log.info("사용자 조회 결과 - ID: {}, 상태: {}, 삭제시간: {}", 
                    user.getUserId(), user.getStatus(), user.getDeletedAt());
        } else {
            log.warn("해당 이메일의 사용자가 없음: {}", email);
            return "redirect:/login?error=not_found";
        }
        
        // 복구 가능 여부 확인
        boolean recoverable = userService.isAccountRecoverable(email);
        log.info("계정 복구 가능 여부: {}", recoverable);
        
        if (!recoverable) {
            if (!"DELETED".equals(user.getStatus())) {
                log.warn("삭제된 계정이 아님: {}", email);
                return "redirect:/login?error=not_deleted";
            } else {
                log.warn("복구 기간(30일) 초과: {}", email);
                return "redirect:/login?error=not_recoverable";
            }
        }
        
        model.addAttribute("email", email);
        model.addAttribute("user", user);
        return "account/recover";
    }
    
    /**
     * 계정 복구 처리
     */
    @PostMapping("/recover")
    public String recoverAccount(@RequestParam String email, 
                                RedirectAttributes redirectAttributes,
                                HttpServletRequest request) {
        log.info("계정 복구 처리 요청 - 이메일: {}", email);
        
        // 사용자 정보 조회
        Users user = userService.getUserByEmail(email);
        if (user != null) {
            log.info("복구 전 사용자 정보 - 상태: {}, 삭제시간: {}", 
                    user.getStatus(), user.getDeletedAt());
        } else {
            log.warn("해당 이메일의 사용자가 없음: {}", email);
            redirectAttributes.addFlashAttribute("error", "계정을 찾을 수 없습니다.");
            return "redirect:/login?error=not_found";
        }
        
        // 계정 복구 처리
        boolean recovered = userService.recoverAccount(email);
        log.info("계정 복구 결과: {}", recovered);
        
        // 세션에서 복구 관련 속성 제거
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute("lastLoginEmail");
            session.removeAttribute("oauth2Email");
            session.removeAttribute("deletedAccount");
            session.removeAttribute("showRecoveryLink");
        }
        
        if (recovered) {
            redirectAttributes.addFlashAttribute("message", "계정이 성공적으로 복구되었습니다. 로그인해주세요.");
            return "redirect:/login?recovered=true";
        } else {
            redirectAttributes.addFlashAttribute("error", "계정 복구에 실패했습니다.");
            return "redirect:/login?error=recovery_failed";
        }
    }
}