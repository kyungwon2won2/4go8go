package com.example.demo.mapper;

import com.example.demo.domain.user.model.UserAuth;
import com.example.demo.domain.user.model.Users;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface UserMapper {

    // 로그인 사용자 인증
    public Users login(String username);

    // 회원가입
    public int join(Users user) throws Exception;

    // 회원 권한 등록
    public int insertAuth(UserAuth userAuth) throws Exception;
    
    // 오늘 생일인 사용자 목록 조회
    public List<Users> findUsersByBirthdayToday();
}
