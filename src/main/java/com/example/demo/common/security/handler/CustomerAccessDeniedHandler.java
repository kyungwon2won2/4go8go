package com.example.demo.common.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

@Slf4j
public class CustomerAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.info("접근 거부 에러 처리");

        // 응답 상태코드
        int statusCode = response.getStatus();
        
        // 에러 객체를 request에 저장하여 전달 (에러 메시지 형태로)
        request.setAttribute("message", "접근 권한이 없습니다.");
        request.setAttribute("status", 403);
        
        // /error 경로로 포워딩 (Thymeleaf 템플릿으로 처리)
        request.getRequestDispatcher("/error").forward(request, response);
    }
}
