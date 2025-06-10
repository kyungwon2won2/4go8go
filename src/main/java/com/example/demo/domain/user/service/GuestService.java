package com.example.demo.domain.user.service;

import com.example.demo.domain.user.dto.CreateUserDTO;
import com.example.demo.domain.user.model.UserRole;
import com.example.demo.domain.user.model.Users;
import com.example.demo.mapper.UserMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuestService {

    private final PasswordEncoder passwordEncoder;

    private final UserMapper userMapper;

    @Transactional
    public void createUser(CreateUserDTO dto) throws Exception {
        Users user = new Users();
        user.setEmail(dto.email());
        user.setPassword(dto.password());
        user.setName(dto.name());
        user.setPhone(dto.phone());
        user.setNickname(dto.nickname());
        user.setAddress(dto.address());
        user.setEmailVerified(dto.receiveMail());

        if (dto.birthDate() != null) {
            Date birthDate = Date.from(dto.birthDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
            user.setBirthDate(birthDate);
        }

        user.setReceiveMail(dto.receiveMail());

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
}
