package com.example.demo.common.security.handler;


import com.example.demo.domain.user.model.CustomerUser;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

//로그인 성공 처리 클래스
@Slf4j
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                        HttpServletResponse response, 
                                        Authentication authentication) throws IOException {

        CustomerUser user = (CustomerUser)authentication.getPrincipal();

        // 사용자 권한에 따라 다른 페이지로 리디렉션
//        if (user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
//            response.sendRedirect("/admin");
//        } else if (user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER"))) {
//            response.sendRedirect("/user");
//        } else {
//            response.sendRedirect("/");
//        }
        response.sendRedirect("/");
    }
}
