package com.example.demo.common.security.service;

import com.example.demo.domain.user.model.UserRole;
import com.example.demo.domain.user.model.Users;
import com.example.demo.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

        // 회원 등록
        int result = userMapper.join(user);

        // 마지막으로 삽입된 사용자 ID 조회 (MySQL의 LAST_INSERT_ID() 이용)
        if(result > 0) {
            UserRole userRole = new UserRole();
            // 새로 생성된 사용자 ID는 DB에서 자동 생성됨 (AUTO_INCREMENT)
            // 실제 구현 시에는 userMapper.getLastInsertId() 같은 메서드를 추가하거나
            // MyBatis의 useGeneratedKeys 기능을 사용하는 것이 좋습니다
            userRole.setUserId(user.getUserId());
            userRole.setRoleName("ROLE_USER");
            result += userMapper.insertAuth(userRole);
        }
        return result;
    }

    @Override
    public int insertAuth(UserRole userRole) throws Exception {
        return 0;
    }

    @Override
    public List<Users> getAllUsers() {
        return userMapper.getAllUsers();
    }

    @Override
    public Users getUserById(String userId) {
        return userMapper.getUserById(userId);
    }

    @Override
    public int updateUser(Users user) {
        // 비밀번호가 입력된 경우에만 암호화 처리
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encodedPassword);
        }

        return userMapper.updateUser(user);
    }

    @Override
    public int deleteUser(String userId) {
        return userMapper.deleteUser(userId);
    }
}
