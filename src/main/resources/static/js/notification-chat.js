/**
 * 채팅 알림 관련 기능
 */
(function() {
    'use strict';

    // 초기화
    document.addEventListener('DOMContentLoaded', function() {
        // 캐시된 상태가 있으면 복원
        try {
            const hasChatMessage = localStorage.getItem('hasChatMessage') === 'true';
            if (hasChatMessage) {
                updateChatIcon(true);
            }
        } catch (e) {
            console.warn('채팅 아이콘 상태 복원 실패:', e);
        }

        // 채팅방 페이지인 경우 상태 확인
        if (window.location.pathname.includes('/chat/room/')) {
            console.log('채팅방 페이지 감지됨 - 로딩 후 상태를 초기화합니다');
            // 채팅방에 들어가면 알림 상태 초기화
            updateChatIcon(false);
            
            // 로컬 스토리지에서 상태 제거
            try {
                localStorage.removeItem('hasChatMessage');
            } catch (e) {
                console.warn('채팅 상태 초기화 실패:', e);
            }
        }
    });

    // 채팅 아이콘 업데이트
    function updateChatIcon(hasNew) {
        console.log('채팅 아이콘 업데이트:', hasNew);
        const chatIcon = document.getElementById('chatMessageIcon');
        const chatBadge = document.getElementById('chatMessageBadge');

        if (chatIcon) {
            if (hasNew) {
                chatIcon.classList.add('has-new');
                
                // 채팅 뱃지 표시
                if (chatBadge) {
                    chatBadge.style.display = 'block';
                }
            } else {
                chatIcon.classList.remove('has-new');
                
                // 채팅 뱃지 숨기기
                if (chatBadge) {
                    chatBadge.style.display = 'none';
                }
            }
        }

        // 상태 저장 (페이지 전환 시 유지)
        try {
            if (hasNew) {
                localStorage.setItem('hasChatMessage', 'true');
            } else {
                localStorage.removeItem('hasChatMessage');
            }
        } catch (e) {
            console.warn('채팅 상태 저장 실패:', e);
        }
    }

    // 전역으로 노출
    window.NotificationChat = {
        updateChatIcon: updateChatIcon
    };
})();