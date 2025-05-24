package com.example.demo.common.oauth.handler;

import com.example.demo.domain.user.model.CustomerUser;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

/**
 * OAuth2 로그인 성공 핸들러
 * 소셜 로그인 성공 시 처리를 담당
 */
@Slf4j
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        log.info("소셜 로그인 성공: {}", authentication.getName());

        // 사용자 상태 확인
        if (authentication.getPrincipal() instanceof CustomerUser) {
            CustomerUser user = (CustomerUser) authentication.getPrincipal();
            log.info("권한: {}", user.getAuthorities());

            // 사용자의 상태가 DELETED인 경우 로그인 페이지로 리디렉션하고 메시지 추가
            if ("DELETED".equals(user.getUser().getStatus())) {
                log.warn("탈퇴한 계정으로 로그인 시도: {}", authentication.getName());
                request.getSession().setAttribute("loginError", "탈퇴한 회원입니다.");
                response.sendRedirect("/login?error=deleted");
                return;
            }

            // 모든 사용자를 홈페이지로 리디렉션
            response.sendRedirect("/");
        } else {
            // 기본적으로 메인 페이지로 리디렉션
            response.sendRedirect("/");
        }
    }
}