package com.example.demo.common.security.service;

import com.example.demo.domain.user.model.EmailVerification;
import com.example.demo.domain.user.model.UserRole;
import com.example.demo.domain.user.model.Users;

import java.util.List;

public interface UserService {

    //로그인 사용자 인증
    public Users login(String username);

    //회원가입
    public int join(Users user) throws Exception;

    //회원 권한 등록
    public int insertAuth(UserRole userRole) throws Exception;

    // 모든 사용자 조회
    public List<Users> getAllUsers();

    // ID로 사용자 조회
    public Users getUserById(String userId);

    // 이메일로 사용자 조회
    public Users getUserByEmail(String email);

    // 사용자 정보 수정
    public int updateUser(Users user);

    // 사용자 삭제
    public int deleteUser(String userId);
    
    // 이메일 인증 토큰 생성 및 저장
    public String createEmailVerification(String email);
    
    // 이메일 인증 처리
    public boolean verifyEmail(String token);
    
    // 이메일 인증 토큰 조회
    public EmailVerification getEmailVerification(String token);
}