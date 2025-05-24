-- 계정 탈퇴 시간 필드 추가
ALTER TABLE user ADD COLUMN deleted_at DATETIME DEFAULT NULL;

-- 인덱스 추가 (성능 개선을 위해)
CREATE INDEX idx_user_status_deleted_at ON user(status, deleted_at);

-- 기존 DELETED 상태 계정에 현재 시간 설정 (선택 사항)
UPDATE user SET deleted_at = NOW() WHERE status = 'DELETED';