-- 기존 deleted_at이 null인 탈퇴 계정들 완전 삭제
-- 주의: 이 스크립트는 데이터를 영구적으로 삭제합니다!

-- 1. 삭제할 계정들 확인 (실행 전 확인용)
SELECT user_id, email, name, status, deleted_at, created_at
FROM user 
WHERE status = 'DELETED' AND deleted_at IS NULL;

-- 2. 해당 계정들의 권한 정보 먼저 삭제
DELETE FROM user_role 
WHERE user_id IN (
    SELECT user_id FROM user 
    WHERE status = 'DELETED' AND deleted_at IS NULL
);

-- 3. 해당 계정들 완전 삭제
DELETE FROM user 
WHERE status = 'DELETED' AND deleted_at IS NULL;

-- 4. 삭제 결과 확인
SELECT COUNT(*) as remaining_deleted_accounts
FROM user 
WHERE status = 'DELETED';

-- 5. 정리 완료 후 상태 확인
SELECT 
    COUNT(CASE WHEN status = 'ACTIVE' THEN 1 END) as active_users,
    COUNT(CASE WHEN status = 'DELETED' AND deleted_at IS NOT NULL THEN 1 END) as properly_deleted_users,
    COUNT(CASE WHEN status = 'DELETED' AND deleted_at IS NULL THEN 1 END) as problematic_deleted_users
FROM user;
