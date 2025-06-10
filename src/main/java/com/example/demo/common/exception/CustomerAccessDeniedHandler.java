package com.example.demo.common.exception;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class CustomerAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.info("접근 거부 에러 처리 : {}", accessDeniedException.getMessage());

        //Ajax 요청인지 확인(일반적으로 XMLHttpRequest 헤더 검사)
        String ajaxHeader = request.getHeader("X-Requested-With");
        boolean isAjax = "XMLHttpRequest".equals(ajaxHeader);

        if (isAjax) {
            // Ajax 요청 시 JSON 응답으로 401 Unauthorized 처리
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);    //401
            String json = "{\"success\":false,\"message\":\"로그인이 필요합니다.\",\"code\":\"LOGIN_REQUIRED\"}";
            response.getWriter().write(json);
            response.getWriter().flush();
            return;
        } else {
            // 일반 요청일 경우 로그인 페이지로 리다이렉트
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
    }

}
