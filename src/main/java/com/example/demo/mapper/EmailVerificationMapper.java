package com.example.demo.mapper;

import com.example.demo.common.email.model.EmailVerification;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmailVerificationMapper {

    void insert(EmailVerification verification) ;

    EmailVerification selectByEmail(String email);

    int update(EmailVerification verification);

    void deleteByEmail(String email);

    boolean existsByEmail(String email);
}
