/**
 * 알림 관련 공통 유틸리티 함수
 */
(function() {
    'use strict';

    // 알림 아이콘 색상 업데이트
    function updateNotificationIcon(hasNew) {
        const notificationIcon = document.getElementById('notificationIcon');
        if (notificationIcon) {
            if (hasNew) {
                notificationIcon.classList.add('has-new');
            } else {
                notificationIcon.classList.remove('has-new');
            }
        }
    }

    // 알림 뱃지 업데이트
    function updateNotificationBadge(count) {
        const badge = document.getElementById('notificationBadge');
        if (badge) {
            if (count > 0) {
                badge.textContent = count > 99 ? '99+' : count;
                badge.classList.remove('hidden');
            } else {
                badge.classList.add('hidden');
                badge.textContent = '';
            }
        }
    }

    // 알림 개수 로드
    function loadNotificationCount() {
        return fetch('/api/notifications/count')
            .then(response => response.json())
            .then(data => {
                const count = data.count;
                updateNotificationBadge(count);
                updateNotificationIcon(count > 0);
                return count;
            })
            .catch(error => {
                console.error('알림 개수 로드 실패:', error);
                return 0;
            });
    }

    // 브라우저 알림 표시
    function showBrowserNotification(notification) {
        if (Notification.permission === 'granted') {
            const browserNotification = new Notification('4고8고마켓 알림', {
                body: notification.content,
                icon: '/image/4go8go_logo.png'
            });
            
            // 클릭 시 해당 페이지로 이동
            browserNotification.onclick = function() {
                window.focus();
                if (notification.url) {
                    window.location.href = notification.url;
                }
                browserNotification.close();
            };
        }
    }

    // 브라우저 알림 권한 요청
    function requestNotificationPermission() {
        if (Notification.permission !== 'granted' && Notification.permission !== 'denied') {
            Notification.requestPermission().then(permission => {
                console.log('알림 권한 상태:', permission);
            });
        }
    }

    // 타임스탬프 형식화
    function formatTimestamp(timestamp) {
        const date = new Date(timestamp);
        const now = new Date();
        const diffMs = now - date;
        
        // 1분 이내
        if (diffMs < 60 * 1000) {
            return '방금 전';
        }
        
        // 1시간 이내
        if (diffMs < 60 * 60 * 1000) {
            const minutes = Math.floor(diffMs / (60 * 1000));
            return `${minutes}분 전`;
        }
        
        // 오늘 내
        if (date.toDateString() === now.toDateString()) {
            const hours = date.getHours().toString().padStart(2, '0');
            const minutes = date.getMinutes().toString().padStart(2, '0');
            return `오늘 ${hours}:${minutes}`;
        }
        
        // 어제
        const yesterday = new Date(now);
        yesterday.setDate(now.getDate() - 1);
        if (date.toDateString() === yesterday.toDateString()) {
            const hours = date.getHours().toString().padStart(2, '0');
            const minutes = date.getMinutes().toString().padStart(2, '0');
            return `어제 ${hours}:${minutes}`;
        }
        
        // 그 외
        const year = date.getFullYear();
        const month = (date.getMonth() + 1).toString().padStart(2, '0');
        const day = date.getDate().toString().padStart(2, '0');
        return `${year}-${month}-${day}`;
    }

    // CSRF 토큰 가져오기
    function getCsrfToken() {
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content') || '';
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') || 'X-CSRF-TOKEN';
        
        const headers = {
            'Content-Type': 'application/json'
        };
        
        if (csrfToken && csrfHeader) {
            headers[csrfHeader] = csrfToken;
        }
        
        return headers;
    }

    // 전역으로 노출
    window.NotificationUtils = {
        updateNotificationIcon: updateNotificationIcon,
        updateNotificationBadge: updateNotificationBadge,
        loadNotificationCount: loadNotificationCount,
        showBrowserNotification: showBrowserNotification,
        requestNotificationPermission: requestNotificationPermission,
        formatTimestamp: formatTimestamp,
        getCsrfToken: getCsrfToken
    };
})();