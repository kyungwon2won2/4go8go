package com.example.demo.common.oauth.handler;

import com.example.demo.domain.user.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final UserService userService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        
        log.error("OAuth2 인증 실패 - 예외 타입: {}, 메시지: {}", exception.getClass().getSimpleName(), exception.getMessage());
        
        HttpSession session = request.getSession();
        
        // 소셜 로그인 시 저장된 이메일 확인
        String email = (String) session.getAttribute("oauth2Email");
        
        // 세션에서 설정된 loginError 확인
        String sessionLoginError = (String) session.getAttribute("loginError");
        log.error("세션의 loginError: {}", sessionLoginError);
        
        // 정지된 회원 처리 - 다양한 케이스 확인
        if ((exception.getMessage() != null && exception.getMessage().contains("정지된")) ||
            (sessionLoginError != null && sessionLoginError.contains("정지된"))) {
            log.error("정지된 계정으로 리다이렉트");
            session.setAttribute("loginError", "정지된 계정입니다. 관리자에게 문의하세요.");
            response.sendRedirect("/login?error=suspended");
            return;
        }
        
        // 탈퇴한 회원인 경우 - 복구 가능 여부 확인
        if (exception.getMessage() != null && exception.getMessage().contains("탈퇴한 회원")) {
            if (email != null && userService.isAccountRecoverable(email)) {
                // 복구 가능한 경우 복구 페이지로 리다이렉트
                response.sendRedirect("/account/recover?email=" + email);
                return;
            }
            
            session.setAttribute("loginError", "탈퇴한 회원입니다.");
            response.sendRedirect("/login?error=deleted");
            return;
        }
        
        // 정지된 회원인 경우
        if (exception.getMessage() != null && exception.getMessage().contains("정지된 계정")) {
            session.setAttribute("loginError", "정지된 계정입니다. 관리자에게 문의하세요.");
            response.sendRedirect("/login?error=suspended");
            return;
        }
        
        // InternalAuthenticationServiceException 처리 추가
        if (exception instanceof InternalAuthenticationServiceException) {
            InternalAuthenticationServiceException internalEx = (InternalAuthenticationServiceException) exception;
            
            if (internalEx.getMessage().contains("정지된 계정")) {
                session.setAttribute("loginError", "정지된 계정입니다. 관리자에게 문의하세요.");
                response.sendRedirect("/login?error=suspended");
                return;
            }
            
            if (internalEx.getMessage().contains("탈퇴한 회원")) {
                session.setAttribute("loginError", "탈퇴한 회원입니다.");
                response.sendRedirect("/login?error=deleted");
                return;
            }
        }
        
        // 세션에서 직접 설정된 오류 메시지 확인
        if (session.getAttribute("deletedAccount") != null) {
            response.sendRedirect("/login?error=deleted");
            return;
        }
        
        // 원인 예외 메시지 확인 
        if (exception.getCause() != null) {
            String causeMessage = exception.getCause().getMessage();
            
            if (causeMessage != null && causeMessage.contains("탈퇴한 회원")) {
                session.setAttribute("loginError", "탈퇴한 회원입니다.");
                response.sendRedirect("/login?error=deleted");
                return;
            }
            
            if (causeMessage != null && causeMessage.contains("정지된 계정")) {
                session.setAttribute("loginError", "정지된 계정입니다. 관리자에게 문의하세요.");
                response.sendRedirect("/login?error=suspended");
                return;
            }
        }
        
        // 추가 정보 입력이 필요한 경우
        if ((exception.getMessage() != null && exception.getMessage().contains("추가 정보 입력")) ||
            session.getAttribute("requireAdditionalInfo") != null) {
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