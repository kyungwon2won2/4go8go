package com.example.demo.domain.user.service;

import com.example.demo.domain.user.dto.UpdateUserDTO;
import com.example.demo.domain.user.model.Users;
import com.example.demo.mapper.UserMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    public List<Users> getAllUsers() {
        return userMapper.getAllUsers();
    }

    public Users getUserById(String userId) {
        return userMapper.getUserById(userId);
    }

    @Transactional
    public int updateUser(UpdateUserDTO dto) {
        Users user = userMapper.getUserByEmail(dto.email());
        user.setEmail(dto.email());
        user.setName(dto.name());
        user.setNickname(dto.nickname());
        user.setPhone(dto.phone());
        user.setAddress(dto.address());
        user.setReceiveMail(dto.receiveMail());  // receiveMail 필드 추가

        // 비밀번호 처리 수정
        if (dto.password() != null && !dto.password().isEmpty()) {
            String encodedPassword = passwordEncoder.encode(dto.password());
            user.setPassword(encodedPassword);
        }

        return userMapper.updateUser(user);
    }

    @Transactional
    public int deleteUser(String email) {
        Users user = userMapper.getUserByEmail(email);
        user.setStatus("DELETED");
        return userMapper.updateUser(user);
    }

    public Users getUserByEmail(String email){
        return userMapper.getUserByEmail(email);
    }
}
