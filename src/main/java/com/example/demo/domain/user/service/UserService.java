package com.example.demo.domain.user.service;

import com.example.demo.domain.user.dto.TestDto;
import com.example.demo.domain.user.model.UserRole;
import com.example.demo.domain.user.model.Users;
import com.example.demo.mapper.UserMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void createUser(TestDto testDto) throws Exception {

        Users user = new Users();
        user.setEmail(testDto.getEmail());
        user.setPassword(testDto.getPassword());
        user.setName(testDto.getName());
        user.setPhone(testDto.getPhone());
        user.setNickname(testDto.getNickname());
        user.setAddress(testDto.getAddress());

        user.setEmailVerified(true);

        if (testDto.getBirthDate() != null) {
            Date birthDate = Date.from(testDto.getBirthDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
            user.setBirthDate(birthDate);
        }

        user.setReceiveMail(testDto.isReceiveMail());

        UserRole role = new UserRole();
        role.setRoleName("ROLE_USER");

        List<UserRole> roleList = new ArrayList<>();
        roleList.add(role);
        user.setRoleList(roleList);

        String password = user.getPassword();
        String encodedPassword = passwordEncoder.encode(password);
        user.setPassword(encodedPassword);

        int result = userMapper.join(user);

        if(result > 0) {
            UserRole userRole = new UserRole();

            userRole.setUserId(user.getUserId());
            userRole.setRoleName("ROLE_USER");
            userMapper.insertAuth(userRole);
        }
    }

    public List<Users> getAllUsers() {
        return userMapper.getAllUsers();
    }

    public Users getUserById(String userId) {
        return userMapper.getUserById(userId);
    }

    public int updateUser(Users user) {
        // 비밀번호가 입력된 경우에만 암호화 처리
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encodedPassword);
        }

        return userMapper.updateUser(user);
    }

    public int deleteUser(String userId) {
        return userMapper.deleteUser(userId);
    }
}
