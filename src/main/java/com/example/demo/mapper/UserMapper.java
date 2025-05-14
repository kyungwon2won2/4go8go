package com.example.demo.mapper;

import com.example.demo.domain.user.model.EmailVerification;
import com.example.demo.domain.user.model.UserRole;
import com.example.demo.domain.user.model.Users;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {

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

    // 오늘 생일인 사용자 목록 조회
    List<Users> findUsersByBirthdayToday();
    
    // 이메일 인증 토큰 저장
    public void saveEmailVerification(EmailVerification verification);
    
    // 이메일 인증 토큰 조회
    public EmailVerification getEmailVerification(String token);
    
    // 이메일 인증 완료 처리
    public int updateEmailVerification(EmailVerification verification);
    
    // 사용자 이메일 인증 상태 업데이트
    public int updateUserEmailVerified(String email, boolean verified);
}
