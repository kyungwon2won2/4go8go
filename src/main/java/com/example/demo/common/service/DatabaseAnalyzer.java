package com.example.demo.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 데이터베이스 분석 서비스
 * 필요시 수동으로 DB 구조 분석 가능
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseAnalyzer {
    
    private final JdbcTemplate jdbcTemplate;
    
    /**
     * user 테이블을 참조하는 외래키 분석
     */
    public void analyzeUserReferences() {
        try {
            log.info("=== 데이터베이스 구조 분석 시작 ===");
            
            String sql = """
                SELECT 
                    TABLE_NAME,
                    COLUMN_NAME,
                    CONSTRAINT_NAME,
                    REFERENCED_TABLE_NAME,
                    REFERENCED_COLUMN_NAME
                FROM information_schema.KEY_COLUMN_USAGE 
                WHERE REFERENCED_TABLE_NAME = 'user'
                AND TABLE_SCHEMA = DATABASE()
                """;
            
            List<Map<String, Object>> foreignKeys = jdbcTemplate.queryForList(sql);
            
            log.info("user 테이블을 참조하는 외래키 수: {}", foreignKeys.size());
            
            for (Map<String, Object> fk : foreignKeys) {
                log.info("테이블: {}, 컬럼: {}, 제약조건명: {}", 
                        fk.get("TABLE_NAME"), 
                        fk.get("COLUMN_NAME"), 
                        fk.get("CONSTRAINT_NAME"));
            }
            
            log.info("=== 데이터베이스 구조 분석 완료 ===");
            
        } catch (Exception e) {
            log.error("데이터베이스 분석 중 오류 발생", e);
        }
    }
}
