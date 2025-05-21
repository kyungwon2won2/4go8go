package com.example.demo.common.email.dto;

public record EmailRequest(
        String email,
        String code
) {
}
