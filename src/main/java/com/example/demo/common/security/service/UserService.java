package com.example.demo.common.security.service;

import com.example.demo.domain.user.model.Users;

public interface UserService {

       //로그인 사용자 인증
    public Users login(String username);

    public int join(Users user) throws Exception;
}
