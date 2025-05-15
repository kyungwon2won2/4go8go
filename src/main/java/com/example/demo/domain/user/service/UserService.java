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

        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encodedPassword);
        }

        return userMapper.updateUser(user);
    }

    public int deleteUser(String userId) {
        return userMapper.deleteUser(userId);
    }

    public Users getUserByEmail(String email){
        return userMapper.getUserByEmail(email);
    }
}
