package com.example.demo.common.exception;

import com.example.demo.common.stringcode.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j(topic = "GlobalExceptionHandler")
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 사용자 정의 예외 처리
    @ExceptionHandler(InvalidConditionException.class)
    public ModelAndView handleInvalidConditionException(InvalidConditionException ex, HttpServletRequest request) {
        log.error("InvalidConditionException: {}, Request URI: {}", ex.getMessage(), request.getRequestURI());
        return buildErrorModelAndView(
                ErrorCode.VALIDATION_FAILED,
                ex.getMessage(), 
                request.getRequestURI(),
                ex
        );
    }

    // NullPointerException 처리
    @ExceptionHandler(NullPointerException.class)
    public ModelAndView handleNullPointerException(NullPointerException ex, HttpServletRequest request) {
        log.error("NullPointerException: {}, Request URI: {}", ex.getMessage(), request.getRequestURI(), ex);

        // loginUser 가 null일 경우
        if (ex.getMessage() != null && ex.getMessage().contains("loginUser")) {
            return buildErrorModelAndView(
                    ErrorCode.LOGIN_REQUIRED,
                    ex.getMessage(),
                    request.getRequestURI(),
                    ex
            );
        }

        // 일반 NullPointerException 처리
        return buildErrorModelAndView(
                ErrorCode.INTERNAL_SERVER_ERROR,
                ex.getMessage(),
                request.getRequestURI(),
                ex
        );
    }

    // IllegalArgumentException 처리
    @ExceptionHandler(IllegalArgumentException.class)
    public ModelAndView handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        log.error("IllegalArgumentException: {}, Request URI: {}", ex.getMessage(), request.getRequestURI());
        return buildErrorModelAndView(
                ErrorCode.INVALID_PARAMETER,
                ex.getMessage(),
                request.getRequestURI(),
                ex
        );
    }

    // IllegalAccessException 처리
    @ExceptionHandler(IllegalAccessException.class)
    public ModelAndView handleIllegalAccessException(IllegalAccessException ex, HttpServletRequest request) {
        log.error("IllegalAccessException: {}, Request URI: {}", ex.getMessage(), request.getRequestURI());
        return buildErrorModelAndView(
                ErrorCode.FORBIDDEN_ACCESS,
                ex.getMessage(),
                request.getRequestURI(),
                ex
        );
    }

    // IllegalStateException 처리
    @ExceptionHandler(IllegalStateException.class)
    public ModelAndView handleIllegalStateException(IllegalStateException ex, HttpServletRequest request) {
        log.error("IllegalStateException: {}, Request URI: {}", ex.getMessage(), request.getRequestURI());
        return buildErrorModelAndView(
                ErrorCode.INVALID_PARAMETER,
                ex.getMessage(),
                request.getRequestURI(),
                ex
        );
    }

    // 404 Not Found 처리
    @ExceptionHandler(NoHandlerFoundException.class)
    public ModelAndView handleNoHandlerFoundException(NoHandlerFoundException ex, HttpServletRequest request) {
        log.error("Page not found: {}, Request URI: {}", ex.getMessage(), request.getRequestURI());
        return buildErrorModelAndView(
                ErrorCode.RESOURCE_NOT_FOUND,
                "요청하신 페이지를 찾을 수 없습니다.",
                request.getRequestURI(),
                ex
        );
    }

    // 효율성 검사 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ModelAndView handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        StringBuilder stringBuilder = new StringBuilder();
        ex.getBindingResult().getFieldErrors().forEach((error) -> {
            stringBuilder.append(error.getField()).append(": ").append(error.getDefaultMessage()).append(" / ");
        });

        if (stringBuilder.length() > 3) {
            stringBuilder.setLength(stringBuilder.length() - 3);
        }

        String errorMessage = stringBuilder.toString();
        log.error("Validation Failed: {}, Request URI: {}", errorMessage, request.getRequestURI());

        return buildErrorModelAndView(
                ErrorCode.VALIDATION_FAILED,
                errorMessage,
                request.getRequestURI(),
                ex
        );
    }

    // 기타 모든 예외 처리
    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled Exception: {}, Request URI: {}", ex.getMessage(), request.getRequestURI(), ex);
        return buildErrorModelAndView(
                ErrorCode.INTERNAL_SERVER_ERROR,
                "처리 중 예상치 못한 오류가 발생했습니다.",
                request.getRequestURI(),
                ex
        );
    }

    // ModelAndView 구성 메서드
    private ModelAndView buildErrorModelAndView(ErrorCode errorCode, String message, String path, Exception ex) {
        ModelAndView modelAndView = new ModelAndView("errorPage");

        // 기본 에러 정보
        modelAndView.addObject("timestamp", LocalDateTime.now().format(formatter));
        modelAndView.addObject("status", errorCode.getStatus().value());
        modelAndView.addObject("error", errorCode.getStatus().getReasonPhrase());
        modelAndView.addObject("code", errorCode.name());
        modelAndView.addObject("message", errorCode.getMessage());
        modelAndView.addObject("detailMessage", message);
        modelAndView.addObject("path", path);
        // 개발 환경을 위한 추가 디버그 정보
        if (ex != null) {
            modelAndView.addObject("exception", ex.getClass().getName());
            modelAndView.addObject("trace", ex.getStackTrace().length > 0 ? ex.getStackTrace()[0].toString() : "");
        }
        modelAndView.setStatus(errorCode.getStatus());
        return modelAndView;
    }
}
