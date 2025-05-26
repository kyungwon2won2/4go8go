package com.example.demo.domain.user.service;

import com.example.demo.domain.user.dto.UpdateUserDTO;
import com.example.demo.domain.user.model.Users;
import com.example.demo.mapper.UserMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 사용자 서비스
 * 일반 사용자 관련 비즈니스 로직 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    /**
     * 모든 사용자 조회
     */
    public List<Users> getAllUsers() {
        return userMapper.getAllUsers();
    }

    /**
     * ID로 사용자 조회
     */
    public Users getUserById(String userId) {
        return userMapper.getUserById(userId);
    }

    // int형 id로 사용자 조회
    public Users getUserById(int userId) {
        return userMapper.findById(userId);
    }

    /**
     * 사용자 정보 업데이트
     */
    @Transactional
    public int updateUser(UpdateUserDTO dto) {
        Users user = userMapper.getUserByEmail(dto.email());
        
        if (user == null) {
            log.error("업데이트할 사용자를 찾을 수 없음: {}", dto.email());
            return 0;
        }
        
        user.setEmail(dto.email());
        user.setName(dto.name());
        user.setNickname(dto.nickname());
        user.setPhone(dto.phone());
        user.setAddress(dto.address());
        user.setReceiveMail(dto.receiveMail());
        user.setUpdatedAt(new Date()); // 업데이트 시간 설정

        // 비밀번호 처리 수정
        if (dto.password() != null && !dto.password().isEmpty()) {
            String encodedPassword = passwordEncoder.encode(dto.password());
            user.setPassword(encodedPassword);
        }

        return userMapper.updateUser(user);
    }

    /**
     * 사용자 소프트 삭제 (상태를 DELETED로 변경)
     */
    @Transactional
    public int deleteUser(String email) {
        Users user = userMapper.getUserByEmail(email);
        
        if (user == null) {
            log.error("삭제할 사용자를 찾을 수 없음: {}", email);
            return 0;
        }
        
        user.setStatus("DELETED");
        user.setDeletedAt(new Date()); // 탈퇴 시간 설정
        
        return userMapper.updateUser(user);
    }

    /**
     * 이메일로 사용자 조회
     */
    public Users getUserByEmail(String email) {
        return userMapper.getUserByEmail(email);
    }
    
    /**
     * 사용자가 탈퇴한 계정인지 확인
     */
    public boolean isDeletedUser(String email) {
        Users user = userMapper.getUserByEmail(email);
        return user != null && "DELETED".equals(user.getStatus());
    }
    
    /**
     * 계정 복구 가능 여부 확인 (30일 이내 탈퇴한 계정만 복구 가능)
     */
    public boolean isAccountRecoverable(String email) {
        Users user = userMapper.getUserByEmail(email);
        
        log.info("계정 복구 가능 여부 확인 - 이메일: {}", email);
        
        if (user == null) {
            log.warn("사용자를 찾을 수 없음: {}", email);
            return false;
        }
        
        if (!"DELETED".equals(user.getStatus())) {
            log.warn("삭제된 계정이 아님 - 상태: {}", user.getStatus());
            return false;
        }
        
        // 삭제 시간이 없는 경우 (이전 버전 데이터)
        if (user.getDeletedAt() == null) {
            log.info("삭제 시간 정보 없음, 복구 가능한 것으로 처리");
            return true;
        }
        
        // 30일 이내 탈퇴한 계정만 복구 가능
        long deletedDays = (new Date().getTime() - user.getDeletedAt().getTime()) / (1000 * 60 * 60 * 24);
        log.info("탈퇴 후 경과 일수: {}", deletedDays);
        
        return deletedDays <= 30;
    }
    
    /**
     * 계정 복구 처리
     */
    @Transactional
    public boolean recoverAccount(String email) {
        Users user = userMapper.getUserByEmail(email);
        
        log.info("계정 복구 시도 - 이메일: {}", email);
        
        if (user == null) {
            log.warn("복구할 사용자를 찾을 수 없음: {}", email);
            return false;
        }
        
        if (!"DELETED".equals(user.getStatus())) {
            log.warn("삭제된 계정이 아님 - 상태: {}", user.getStatus());
            return false;
        }
        
        // 복구 가능 기간 확인 (30일)
        if (user.getDeletedAt() != null) {
            long deletedDays = (new Date().getTime() - user.getDeletedAt().getTime()) / (1000 * 60 * 60 * 24);
            log.info("탈퇴 후 경과 일수: {}", deletedDays);
            
            if (deletedDays > 30) {
                log.warn("복구 기간(30일) 초과");
                return false;
            }
        } else {
            log.info("삭제 시간 정보 없음, 복구 진행");
        }
        
        // 계정 상태 복구
        user.setStatus("ACTIVE");
        user.setDeletedAt(null);
        
        int result = userMapper.updateUser(user);
        log.info("계정 복구 결과 (DB 업데이트): {}", result > 0 ? "성공" : "실패");
        
        return result > 0;
    }
}