package com.example.demo.common.exception.custom;

import com.example.demo.common.stringcode.ErrorCode;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage()); // 부모 클래스 메시지 설정
        this.errorCode = errorCode;
    }
}
