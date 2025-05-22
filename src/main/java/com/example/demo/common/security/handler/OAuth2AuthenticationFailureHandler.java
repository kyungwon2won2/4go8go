package com.example.demo.common.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Slf4j
@Component
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        log.error("OAuth2 인증 실패: {}", exception.getMessage());

        // 탈퇴한 회원인 경우 특별히 처리
        if (exception.getMessage() != null && exception.getMessage().contains("탈퇴한 회원")) {
            request.getSession().setAttribute("loginError", "탈퇴한 회원입니다.");
            response.sendRedirect("/login?error=deleted");
            return;
        }

        // 그 외 다른 오류의 경우
        request.getSession().setAttribute("loginError", "소셜 로그인에 실패했습니다.");
        response.sendRedirect("/login?error=social");
    }
}