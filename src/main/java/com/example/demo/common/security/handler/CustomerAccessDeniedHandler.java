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

        int statusCode = response.getStatus(); //응답 상태코드
        log.info("HTTP 응답 상태 코드 : " + statusCode);
        log.info("accessDeniedException : " + accessDeniedException);

        response.sendRedirect("/exception");
    }
}
