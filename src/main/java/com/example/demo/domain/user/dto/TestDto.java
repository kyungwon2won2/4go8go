package com.example.demo.domain.user.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class TestDto {
    private String email;
    private String password;
    private String nickname;
    private String address;
    private LocalDate birthDate; // Date에서 LocalDate로 변경
    private boolean receiveMail;
}