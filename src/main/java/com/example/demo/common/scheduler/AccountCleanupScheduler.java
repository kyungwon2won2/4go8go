package com.example.demo.common.scheduler;

import com.example.demo.domain.user.model.Users;
import com.example.demo.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 계정 정리 스케줄러
 * 30일 지난 탈퇴 계정들을 자동으로 완전 삭제
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccountCleanupScheduler {
    
    private final UserMapper userMapper;
    
    /**
     * 매일 새벽 2시에 만료된 탈퇴 계정들 완전 삭제
     * cron = "초 분 시 일 월 요일"
     * 0 0 2 * * ? = 매일 새벽 2시 0분 0초
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void deleteExpiredAccounts() {
        log.info("=== 만료된 탈퇴 계정 정리 작업 시작 ===");
        
        try {
            // 30일 지난 탈퇴 계정들 조회
            List<Users> expiredAccounts = userMapper.findExpiredDeletedAccounts();
            
            if (expiredAccounts.isEmpty()) {
                log.info("삭제할 만료된 계정이 없습니다.");
                return;
            }
            
            log.info("삭제 대상 계정 수: {}", expiredAccounts.size());
            
            int deletedCount = 0;
            for (Users user : expiredAccounts) {
                try {
                    // 관련 데이터 먼저 삭제 (외래키 제약조건 때문)
                    userMapper.deleteUserRolesByUserId(user.getUserId());

                    // 사용자 계정 완전 삭제
                    userMapper.permanentlyDeleteUser(user.getUserId());

                    deletedCount++;
                    log.info("계정 완전 삭제 완료 - ID: {}, 이메일: {}, 탈퇴일: {}",
                            user.getUserId(), user.getEmail(), user.getDeletedAt());

                } catch (Exception e) {
                    log.error("계정 삭제 실패 - ID: {}, 이메일: {}, 오류: {}",
                            user.getUserId(), user.getEmail(), e.getMessage());
                }
            }
            
            log.info("=== 탈퇴 계정 정리 작업 완료 - 총 {}개 계정 삭제 ===", deletedCount);
            
        } catch (Exception e) {
            log.error("탈퇴 계정 정리 작업 중 오류 발생", e);
        }
    }
    
    /**
     * 테스트용: 수동 실행 메서드
     * 운영 환경에서는 제거하거나 주석 처리 권장
     */
    public void manualCleanup() {
        log.info("수동 계정 정리 작업 실행");
        deleteExpiredAccounts();
    }
}
