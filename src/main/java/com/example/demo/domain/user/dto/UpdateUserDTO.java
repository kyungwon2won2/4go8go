package com.example.demo.domain.user.dto;

import com.example.demo.util.RegexUtil;
import com.example.demo.util.VerifyUtil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;


public record UpdateUserDTO(
        @NotBlank
        @Length(min = VerifyUtil.MIN_DEFAULT_LENGTH, max = VerifyUtil.MAX_EMAIL_LENGTH)
        @Email(regexp = RegexUtil.REGEXP_EMAIL)
        String email,

        @Length(min = 8, max = VerifyUtil.MAX_DEFAULT_LENGTH)
//        @Pattern(regexp = RegexUtil.REGEXP_PASSWORD, message = "invalid password format")
        String password,
        @NotBlank
        @Length(max = VerifyUtil.MAX_DEFAULT_LENGTH)
        String name,
        @NotBlank
        @Length(max = VerifyUtil.MAX_DEFAULT_LENGTH)
        @Pattern(regexp = RegexUtil.REGEXP_NICKNAME, message = "invalid nickname format")
        String nickname,
        @NotBlank
        @Pattern(regexp = RegexUtil.REGEXP_MOBILE, message = "invalid phone number format")
        String phone,
        @NotBlank
        @Length(max = VerifyUtil.MAX_ADDRESS_LENGTH)
        String address,

        String birthDate
) {
}