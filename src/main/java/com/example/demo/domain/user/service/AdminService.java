package com.example.demo.domain.user.service;

import com.example.demo.domain.user.model.Users;
import com.example.demo.mapper.UserMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserMapper userMapper;

    /**
     * 사용자 상태 업데이트
     * @param userId 사용자 ID
     * @param status 변경할 상태 (ACTIVE, SUSPENDED, DELETED)
     * @return 처리 결과
     */
    @Transactional
    public int updateUserStatus(Integer userId, String status) {
        log.info("사용자 상태 업데이트: userId={}, status={}", userId, status);

        Users user = userMapper.findById(userId);

        if (user == null) {
            log.error("사용자를 찾을 수 없음: {}", userId);
            return 0;
        }

        user.setStatus(status);
        return userMapper.updateUser(user);
    }

    /**
     * ID로 사용자 정보 조회
     */
    public Users getUserById(Integer userId) {
        return userMapper.findById(userId);
    }
}