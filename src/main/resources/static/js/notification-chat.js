/**
 * 채팅 알림 관련 기능
 */
(function () {
    'use strict';

    // 인증 상태 확인 함수
    function isUserAuthenticated() {
        // 로그인 상태 확인 - 헤더에 사용자 정보가 있는지 확인
        const userMenu = document.querySelector('.user-menu');
        if (!userMenu) return false;

        // 로그아웃 버튼이 있으면 로그인 상태
        const logoutForm = userMenu.querySelector('form[action="/logout"]');
        return !!logoutForm;
    }

    // 초기화
    document.addEventListener('DOMContentLoaded', function () {
        // 인증되지 않은 사용자는 채팅 상태 초기화
        if (!isUserAuthenticated()) {
            console.log('비로그인 사용자 - 채팅 상태 초기화');
            updateChatIcon(false);

            // 로컬 스토리지에서 상태 제거
            try {
                localStorage.removeItem('hasChatMessage');
                sessionStorage.removeItem('hasChatMessage');
            } catch (e) {
                console.warn('채팅 상태 초기화 실패:', e);
            }
            return;
        }

        // 로그인 상태에서 초기 채팅 상태 확인
        checkInitialChatStatus();

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

    // 로그인 시 초기 채팅 상태 확인
    function checkInitialChatStatus() {
        console.log('로그인 사용자 - 초기 채팅 상태 확인 중...');

        fetch('/chat/my/rooms')
            .then(response => {
                if (!response.ok) {
                    throw new Error('채팅방 목록 조회 실패');
                }
                return response.json();
            })
            .then(rooms => {
                console.log('채팅방 목록 조회 완료:', rooms);

                // 읽지 않은 메시지가 있는지 확인
                const hasUnreadMessages = rooms && rooms.length > 0 &&
                    rooms.some(room => room.unReadCount && room.unReadCount > 0);

                console.log('읽지 않은 채팅 메시지 존재:', hasUnreadMessages);

                // 채팅 아이콘 상태 업데이트
                updateChatIcon(hasUnreadMessages);
            })
            .catch(error => {
                console.warn('초기 채팅 상태 확인 실패 (무시됨):', error);

                // 캐시된 상태가 있으면 복원 (로그인 상태에서만)
                try {
                    const hasChatMessage = localStorage.getItem('hasChatMessage') === 'true';
                    if (hasChatMessage) {
                        updateChatIcon(true);
                    }
                } catch (e) {
                    console.warn('채팅 아이콘 상태 복원 실패:', e);
                }
            });
    }

    // 채팅 아이콘 업데이트
    function updateChatIcon(hasNew) {
        // 비로그인 사용자는 항상 false로 처리
        if (!isUserAuthenticated()) {
            console.log('비로그인 사용자 - 채팅 아이콘 업데이트 무시');
            hasNew = false;
        }

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

        // 상태 저장 (로그인 상태에서만)
        if (isUserAuthenticated()) {
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
    }

    // 전역으로 노출
    window.NotificationChat = {
        updateChatIcon: updateChatIcon,
        isUserAuthenticated: isUserAuthenticated,
        checkInitialChatStatus: checkInitialChatStatus
    };
})();