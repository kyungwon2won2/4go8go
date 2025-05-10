package com.example.demo.common.security.service;

import com.example.demo.domain.user.model.UserAuth;
import com.example.demo.domain.user.model.Users;

public interface UserService {

       //로그인 사용자 인증
    public Users login(String username);

    //회원가입
    public int join(Users user) throws Exception;

    //회원 권한 등록
    public int insertAuth(UserAuth userAuth) throws Exception;
}
