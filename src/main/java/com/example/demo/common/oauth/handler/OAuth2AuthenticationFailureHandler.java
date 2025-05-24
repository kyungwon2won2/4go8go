package com.example.demo.common.oauth.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

/**
 * OAuth2 로그인 실패 핸들러
 * 소셜 로그인 실패 시 처리를 담당
 */
@Slf4j
@Component
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        log.error("OAuth2 인증 실패: {}", exception.getMessage());
        log.error("예외 클래스: {}", exception.getClass().getName());
        
        HttpSession session = request.getSession();
        
        // 세션에서 직접 설정된 오류 메시지 확인
        if (session.getAttribute("deletedAccount") != null) {
            // 탈퇴한 회원 처리
            log.info("탈퇴한 회원으로 소셜 로그인 시도 (세션 플래그 확인)");
            response.sendRedirect("/login?error=deleted");
            return;
        }
        
        // 예외 메시지나 원인 예외 메시지에서 "탈퇴한 회원" 문자열 검색
        boolean isDeletedMember = false;
        
        // 현재 예외 메시지 확인
        if (exception.getMessage() != null && exception.getMessage().contains("탈퇴한 회원")) {
            isDeletedMember = true;
        }
        
        // 원인 예외 메시지 확인 
        if (!isDeletedMember && exception.getCause() != null) {
            String causeMessage = exception.getCause().getMessage();
            log.error("원인 예외: {}", causeMessage);
            
            if (causeMessage != null && causeMessage.contains("탈퇴한 회원")) {
                isDeletedMember = true;
            }
        }
        
        // 탈퇴한 회원인 경우
        if (isDeletedMember) {
            log.info("탈퇴한 회원으로 소셜 로그인 시도 (예외 메시지 확인)");
            session.setAttribute("loginError", "탈퇴한 회원입니다.");
            response.sendRedirect("/login?error=deleted");
            return;
        }
        
        // 추가 정보 입력이 필요한 경우
        if ((exception.getMessage() != null && exception.getMessage().contains("추가 정보 입력")) ||
            session.getAttribute("requireAdditionalInfo") != null) {
            log.info("추가 정보 입력 페이지로 리디렉션");
            response.sendRedirect("/oauth2/signup/additional-info");
            return;
        }

        // 그 외 다른 오류의 경우
        String errorMessage = "소셜 로그인에 실패했습니다";
        if (exception.getMessage() != null) {
            errorMessage += ": " + exception.getMessage();
        }
        session.setAttribute("loginError", errorMessage);
        response.sendRedirect("/login?error=social");
    }
}