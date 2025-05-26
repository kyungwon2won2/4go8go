package com.example.demo.domain.user.service;

import com.example.demo.domain.user.model.UserRole;
import com.example.demo.domain.user.model.Users;
import com.example.demo.mapper.UserMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        if ("DELETED".equals(status)) {
            user.setDeletedAt(new Date());
        }
        
        int result = userMapper.updateUser(user);
        
        if (result > 0) {
            log.info("사용자 상태 변경 완료: userId={}, status={}", userId, status);
        }
        
        return result;
    }

    /**
     * ID로 사용자 정보 조회
     */
    public Users getUserById(Integer userId) {
        return userMapper.findById(userId);
    }

    /**
     * 이메일로 사용자 정보 조회
     */
    public Users getUserByEmail(String email) {
        return userMapper.findByEmail(email);
    }

    /**
     * 사용자의 권한 목록 조회
     */
    public List<UserRole> getUserRoles(Integer userId) {
        return getUserRolesByUserId(userId);
    }

    /**
     * 권한 통계 조회
     */
    public Map<String, Long> getRoleStatistics() {
        Map<String, Long> stats = new HashMap<>();
        
        List<Users> allUsers = userMapper.getAllUsers();
        
        long userRoleCount = 0;
        long adminRoleCount = 0;
        
        for (Users user : allUsers) {
            List<UserRole> roles = getUserRolesByUserId(user.getUserId());
            for (UserRole role : roles) {
                if ("ROLE_USER".equals(role.getRoleName())) {
                    userRoleCount++;
                } else if ("ROLE_ADMIN".equals(role.getRoleName())) {
                    adminRoleCount++;
                }
            }
        }
        
        stats.put("ROLE_USER", userRoleCount);
        stats.put("ROLE_ADMIN", adminRoleCount);
        
        return stats;
    }

    /**
     * 관리자 권한을 가진 사용자 목록 조회
     */
    public List<Users> getAdminUsers() {
        return getAdminUsersList();
    }

    /**
     * 사용자에게 권한 부여
     */
    @Transactional
    public boolean assignRole(String userEmail, String roleName) {
        try {
            Users user = userMapper.findByEmail(userEmail);
            if (user == null) {
                log.error("사용자를 찾을 수 없음: {}", userEmail);
                return false;
            }

            // 이미 해당 권한이 있는지 확인
            List<UserRole> existingRoles = getUserRolesByUserId(user.getUserId());
            boolean hasRole = existingRoles.stream()
                    .anyMatch(role -> roleName.equals(role.getRoleName()));

            if (hasRole) {
                log.info("사용자가 이미 해당 권한을 보유함: userId={}, role={}", user.getUserId(), roleName);
                return true;
            }

            // 새 권한 부여
            UserRole userRole = new UserRole();
            userRole.setUserId(user.getUserId());
            userRole.setRoleName(roleName);
            userRole.setCreatedAt(new Date());
            userRole.setUpdatedAt(new Date());

            int result = userMapper.insertAuth(userRole);
            log.info("권한 부여 결과: userId={}, role={}, result={}", user.getUserId(), roleName, result);
            
            return result > 0;
        } catch (Exception e) {
            log.error("권한 부여 중 오류 발생", e);
            return false;
        }
    }

    /**
     * 사용자의 권한 회수
     */
    @Transactional
    public boolean revokeRole(Integer userId, String roleName) {
        try {
            int result = deleteUserRole(userId, roleName);
            log.info("권한 회수 결과: userId={}, role={}, result={}", userId, roleName, result);
            return result > 0;
        } catch (Exception e) {
            log.error("권한 회수 중 오류 발생", e);
            return false;
        }
    }

    // 아래는 실제 DB 작업을 위한 private 메서드들입니다.
    // 실제 프로젝트에서는 UserMapper에 이런 메서드들을 추가해주세요.

    private List<UserRole> getUserRolesByUserId(Integer userId) {
        return userMapper.getUserRolesByUserId(userId);
    }

    private List<Users> getAdminUsersList() {
        return userMapper.getAdminUsers();
    }

    private int deleteUserRole(Integer userId, String roleName) {
        return userMapper.deleteUserRole(userId, roleName);
    }
}