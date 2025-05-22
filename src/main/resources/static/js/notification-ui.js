/**
 * 알림 드롭다운 UI 관련 기능
 */
(function() {
    'use strict';
    
    // DOM 요소
    let notificationIcon;
    let notificationBadge;
    let notificationDropdown;
    let notificationList;
    let markAllAsReadBtn;
    
    // 상태 변수
    let isDropdownVisible = false;
    let notifications = [];
    
    // 초기화
    document.addEventListener('DOMContentLoaded', function() {
        // 요소 참조 가져오기
        notificationIcon = document.getElementById('notificationIcon');
        notificationBadge = document.getElementById('notificationBadge');
        notificationDropdown = document.getElementById('notificationDropdown');
        notificationList = document.getElementById('notificationList');
        markAllAsReadBtn = document.getElementById('markAllAsReadBtn');
        
        if (notificationIcon && notificationDropdown) {
            setupEventListeners();
            
            // 페이지 로드 시 알림 개수 로드
            if (window.NotificationUtils) {
                window.NotificationUtils.loadNotificationCount();
            }
        }
    });
    
    // 이벤트 리스너 설정
    function setupEventListeners() {
        // 알림 아이콘 클릭 이벤트
        notificationIcon.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            toggleDropdown();
        });
        
        // 문서 클릭 시 드롭다운 닫기
        document.addEventListener('click', function(e) {
            if (isDropdownVisible && 
                !notificationIcon.contains(e.target) && 
                !notificationDropdown.contains(e.target)) {
                hideDropdown();
            }
        });
        
        // '모두 읽음' 버튼 클릭
        if (markAllAsReadBtn) {
            markAllAsReadBtn.addEventListener('click', function(e) {
                e.stopPropagation();
                markAllAsRead();
            });
        }
    }
    
    // 알림 목록 로드 - 읽지 않은 알림만 표시하도록 수정
    function loadNotifications() {
        console.log('알림 목록 로드 시도');
        if (!notificationList) return;
        
        notificationList.innerHTML = '<div class="notification-loading">알림을 불러오는 중...</div>';
        
        fetch('/api/notifications/unread?page=0&size=5')
            .then(response => {
                console.log('알림 API 응답:', response.status);
                if (!response.ok) {
                    throw new Error('API 응답 오류: ' + response.status);
                }
                return response.json();
            })
            .then(data => {
                console.log('알림 데이터 받음:', data);
                notifications = data;
                updateNotificationList();
            })
            .catch(error => {
                console.error('알림 목록 로드 실패:', error);
                notificationList.innerHTML = '<div class="empty-notification">알림을 불러오는 중 오류가 발생했습니다.</div>';
            });
    }
    
    // 알림 목록 UI 업데이트
    function updateNotificationList() {
        if (!notificationList) return;
        
        // 기존 목록 비우기
        notificationList.innerHTML = '';
        
        if (notifications.length === 0) {
            notificationList.innerHTML = '<div class="empty-notification">새 알림이 없습니다.</div>';
            // 읽지 않은 알림이 없으면 '모두 읽음' 버튼 숨기기
            if (markAllAsReadBtn) {
                markAllAsReadBtn.style.display = 'none';
            }
            return;
        } else {
            // 읽지 않은 알림이 있으면 '모두 읽음' 버튼 표시
            if (markAllAsReadBtn) {
                markAllAsReadBtn.style.display = 'block';
            }
        }
        
        // 알림 항목 추가
        notifications.forEach(notification => {
            const item = document.createElement('div');
            item.className = 'notification-item unread';
            
            // 알림 컨텐츠 컨테이너
            const contentContainer = document.createElement('div');
            contentContainer.className = 'notification-content-container';
            
            // 알림 내용
            const content = document.createElement('div');
            content.className = 'notification-content';
            content.textContent = notification.content;
            
            // 알림 시간
            const time = document.createElement('div');
            time.className = 'notification-time';
            time.textContent = window.NotificationUtils ? 
                               window.NotificationUtils.formatTimestamp(notification.createdAt) : 
                               new Date(notification.createdAt).toLocaleString();
            
            // 컨텐츠 컨테이너에 추가
            contentContainer.appendChild(content);
            contentContainer.appendChild(time);
            
            // 닫기 버튼
            const closeBtn = document.createElement('button');
            closeBtn.className = 'notification-close-btn';
            closeBtn.innerHTML = '<i class="bi bi-x"></i>';
            closeBtn.title = '알림 삭제';
            
            // 닫기 버튼 클릭 이벤트
            closeBtn.addEventListener('click', function(e) {
                e.stopPropagation(); // 부모 요소로 이벤트 전파 방지
                deleteNotification(notification.notificationId, item);
            });
            
            // 아이템에 추가
            item.appendChild(contentContainer);
            item.appendChild(closeBtn);
            
            // 알림 클릭 이벤트 처리
            contentContainer.addEventListener('click', function() {
                // 클릭한 알림 요소 즉시 제거
                if (item.parentNode) {
                    item.parentNode.removeChild(item);
                }
                
                // 알림 읽음 처리
                markAsRead(notification.notificationId);
                
                // 알림 드롭다운 닫기
                hideDropdown();
                
                // 해당 페이지로 즉시 이동
                if (notification.url) {
                    window.location.href = notification.url;
                }
            });
            
            notificationList.appendChild(item);
        });
    }
    
    // 알림 삭제 처리
    function deleteNotification(notificationId, itemElement) {
        const headers = window.NotificationUtils ? 
                        window.NotificationUtils.getCsrfToken() : 
                        { 'Content-Type': 'application/json' };
        
        fetch(`/api/notifications/${notificationId}`, {
            method: 'POST',
            headers: headers,
            credentials: 'same-origin'
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('응답 오류: ' + response.status);
            }
            return response.json();
        })
        .then(data => {
            if (data.status === 'success') {
                // UI에서 삭제
                if (itemElement && itemElement.parentNode) {
                    itemElement.parentNode.removeChild(itemElement);
                }
                
                // 배열에서도 제거
                const index = notifications.findIndex(n => n.notificationId === notificationId);
                if (index !== -1) {
                    notifications.splice(index, 1);
                }
                
                // 알림이 없을 경우 메시지 표시
                if (notifications.length === 0) {
                    notificationList.innerHTML = '<div class="empty-notification">새 알림이 없습니다.</div>';
                    
                    // 모두 읽음 버튼 숨기기
                    if (markAllAsReadBtn) {
                        markAllAsReadBtn.style.display = 'none';
                    }
                }
                
                // 뱃지 업데이트
                if (window.NotificationUtils) {
                    window.NotificationUtils.loadNotificationCount();
                }
            } else {
                console.error('알림 삭제 실패:', data.message);
            }
        })
        .catch(error => {
            console.error('알림 삭제 실패:', error);
        });
    }
    
    // 알림 읽음 처리
    function markAsRead(notificationId) {
        const headers = window.NotificationUtils ? 
                        window.NotificationUtils.getCsrfToken() : 
                        { 'Content-Type': 'application/json' };
        
        fetch(`/api/notifications/${notificationId}/read`, {
            method: 'POST',
            headers: headers,
            credentials: 'same-origin'
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('응답 오류: ' + response.status);
            }
            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('application/json')) {
                return response.json();
            } else {
                return { status: response.ok ? 'success' : 'error' };
            }
        })
        .then(data => {
            // 로컬 목록에서 해당 알림 제거 (드롭다운에서만)
            const index = notifications.findIndex(n => n.notificationId === notificationId);
            if (index !== -1) {
                notifications.splice(index, 1); // 목록에서 제거
                updateNotificationList(); // UI 업데이트
                
                // 알림 개수 업데이트
                if (window.NotificationUtils) {
                    window.NotificationUtils.loadNotificationCount();
                }
            }
        })
        .catch(error => {
            console.error('알림 읽음 처리 실패:', error);
        });
    }
    
    // 모든 알림 읽음 처리
    function markAllAsRead() {
        const headers = window.NotificationUtils ? 
                        window.NotificationUtils.getCsrfToken() : 
                        { 'Content-Type': 'application/json' };
        
        fetch('/api/notifications/read-all', {
            method: 'POST',
            headers: headers,
            credentials: 'same-origin'
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('응답 오류: ' + response.status);
            }
            return response.json();
        })
        .then(data => {
            if (data.status === 'success') {
                // 알림 목록 비우기
                notifications = [];
                notificationList.innerHTML = '<div class="empty-notification">새 알림이 없습니다.</div>';
                
                // 모두 읽음 버튼 숨기기
                if (markAllAsReadBtn) {
                    markAllAsReadBtn.style.display = 'none';
                }
                
                // 뱃지 업데이트
                if (window.NotificationUtils) {
                    window.NotificationUtils.loadNotificationCount();
                }
            }
        })
        .catch(error => {
            console.error('모든 알림 읽음 처리 실패:', error);
        });
    }
    
    // 드롭다운 토글
    function toggleDropdown() {
        console.log('드롭다운 토글:', isDropdownVisible);
        
        if (isDropdownVisible) {
            hideDropdown();
        } else {
            showDropdown();
            loadNotifications();
        }
    }
    
    // 드롭다운 표시
    function showDropdown() {
        if (notificationDropdown) {
            notificationDropdown.style.display = 'block';
            isDropdownVisible = true;
            
            // 새 알림 표시 제거
            if (notificationIcon) {
                notificationIcon.classList.remove('has-new');
            }
        }
    }
    
    // 드롭다운 숨기기
    function hideDropdown() {
        if (notificationDropdown) {
            notificationDropdown.style.display = 'none';
            isDropdownVisible = false;
        }
    }

    // 전역으로 노출
    window.NotificationUI = {
        loadNotifications: loadNotifications,
        markAsRead: markAsRead,
        deleteNotification: deleteNotification,
        markAllAsRead: markAllAsRead,
        toggleDropdown: toggleDropdown
    };
})();