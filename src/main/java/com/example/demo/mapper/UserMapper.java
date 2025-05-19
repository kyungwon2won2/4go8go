package com.example.demo.mapper;

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
    
    // Integer ID로 사용자 조회
    public Users findById(Integer userId);

    // 이메일로 사용자 조회
    public Users getUserByEmail(String email);
    
    // 이메일로 사용자 조회 (별칭)
    public Users findByEmail(String email);

    // 이메일로 사용자 조회
    public Users tusgetUserByEmail(String email);

   // 사용자 정보 수정
    public int updateUser(Users user);

    // 사용자 삭제
    public int deleteUser(String userId);

    List<Users> findUsersByBirthdayToday();

}
