// 채팅 아이콘 업데이트 함수
window.updateChatIcon = function(hasNew) {
    console.log('헤더에서 채팅 아이콘 업데이트:', hasNew);
    const chatIcon = document.getElementById('chatMessageIcon');

    if (chatIcon) {
        if (hasNew) {
            chatIcon.classList.add('has-new');
        } else {
            chatIcon.classList.remove('has-new');
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
};

// 페이지 로드 시 실행
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
        console.log('채팅방 페이지 감지됨 - 로딩 후 상태를 확인합니다');
        // 초기화는 채팅방 로드 시 처리됨
    }
});