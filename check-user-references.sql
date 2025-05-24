-- 1. user 테이블을 참조하는 외래키 확인
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE 
WHERE REFERENCED_TABLE_NAME = 'user';

-- 2. 삭제하려는 계정들의 ID 목록 확인
SELECT user_id, email, name 
FROM user 
WHERE status = 'DELETED' AND deleted_at IS NULL;

-- 3. 해당 계정들이 다른 테이블에서 얼마나 참조되는지 확인
-- (실제 테이블명은 위 결과를 보고 수정해야 함)

-- 예시: user_role 테이블 확인
SELECT COUNT(*) as user_role_count
FROM user_role ur
JOIN user u ON ur.user_id = u.user_id
WHERE u.status = 'DELETED' AND u.deleted_at IS NULL;

-- 예시: 댓글 테이블이 있다면 (comment 등)
-- SELECT COUNT(*) FROM comment WHERE user_id IN (
--     SELECT user_id FROM user WHERE status = 'DELETED' AND deleted_at IS NULL
-- );
