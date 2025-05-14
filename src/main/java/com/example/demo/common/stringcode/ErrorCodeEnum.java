package com.example.demo.common.stringcode;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCodeEnum {

    LOGIN_FAIL(HttpStatus.BAD_REQUEST, "로그인 실패"),
    DUPLICATE_USERNAME_EXIST(HttpStatus.BAD_REQUEST, "중복된 사용자가 존재합니다"),
    DUPLICATE_EMAIL_EXIST(HttpStatus.BAD_REQUEST, "중복된 이메일이 존재합니다."),
    POST_NOT_EXIST(HttpStatus.BAD_REQUEST, "존재하지 않는 글입니다"),
    USER_NOT_MATCH(HttpStatus.BAD_REQUEST, "작성자만 수정, 삭제가 가능합니다"),

    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "인증되지 않은 접근입니다"),
    FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN, "접근 권한이 없습니다"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청하신 자원을 찾을 수 없습니다"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않는 HTTP 메소드입니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다"),

    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "잘못된 요청 파라미터입니다"),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "유효성 검사 실패"),
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "데이터베이스 오류가 발생했습니다"),
    TIMEOUT_ERROR(HttpStatus.GATEWAY_TIMEOUT, "서버가 시간 내 응답하지 않았습니다"),

    FILE_UPLOAD_ERROR(HttpStatus.BAD_REQUEST, "파일 업로드 중 오류가 발생했습니다"),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 미디어 타입입니다"),

    ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, "계정이 잠겼습니다"),
    ACCOUNT_DISABLED(HttpStatus.FORBIDDEN, "계정이 비활성화되었습니다");

    private final HttpStatus status;
    private final String message;

    ErrorCodeEnum(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
