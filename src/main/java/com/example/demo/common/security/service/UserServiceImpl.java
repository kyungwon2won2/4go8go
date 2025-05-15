package com.example.demo.common.security.service;

import com.example.demo.domain.user.model.UserRole;
import com.example.demo.domain.user.model.Users;
import com.example.demo.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    @Override
    public Users login(String username) {
        return userMapper.login(username);
    }

    @Override
    @Transactional
    public int join(Users user) throws Exception {
        // 비밀번호 암호화
        String password = user.getPassword();
        String encodedPassword = passwordEncoder.encode(password);
        user.setPassword(encodedPassword);

        //회원등록
        int result = userMapper.join(user);

        if(result > 0){
            UserRole userAuth = new UserRole();
            userAuth.setUserId(user.getUserId());
            userAuth.setRoleName("ROLE_USER");
            result += userMapper.insertAuth(userAuth);
        }
        return result;
    }

}
