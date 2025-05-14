package com.example.demo.common.exception;


import com.example.demo.common.stringcode.ErrorCode;

public class InvalidConditionException extends IllegalArgumentException{

    ErrorCode errorCode;

    public InvalidConditionException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}