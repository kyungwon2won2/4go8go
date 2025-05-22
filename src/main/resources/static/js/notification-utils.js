/**
 * 알림 유틸리티 함수
 */

// 모달 제어 함수 - 전역에 노출
if (typeof window !== 'undefined') {
    // 알림 아이콘 클릭 이벤트 핸들러
    window.openNotificationModal = function(event) {
        if (event) event.preventDefault();
        const modal = document.getElementById('notificationModal');
        if (modal) {
            modal.style.display = 'block';
            window.notificationModalOpen = true;
            
            // 알림 목록 로드
            if (!window.notificationModalInitialized) {
                loadNotifications();
                window.notificationModalInitialized = true;
            } else {
                // 이미 초기화되었으면 목록 갱신
                loadNotifications();
            }
            
            // 알림 아이콘 색상 초기화 (새 알림 표시 제거)
            const notificationIcon = document.getElementById('notificationIcon');
            if (notificationIcon) {
                notificationIcon.classList.remove('has-new');
            }
            
            // 모달 외부 클릭 시 닫기 이벤트
            document.addEventListener('click', handleClickOutside);
        }
    };
    
    // 모달 외부 클릭 시 닫기
    window.handleClickOutside = function(event) {
        const modal = document.getElementById('notificationModal');
        const icon = document.getElementById('notificationIcon');
        
        if (window.notificationModalOpen && 
            modal && icon && 
            !modal.contains(event.target) && 
            !icon.contains(event.target)) {
            window.closeNotificationModal();
        }
    };
    
    // 알림 모달 닫기
    window.closeNotificationModal = function() {
        const modal = document.getElementById('notificationModal');
        if (modal) {
            modal.style.display = 'none';
            window.notificationModalOpen = false;
            
            // 외부 클릭 이벤트 제거
            document.removeEventListener('click', window.handleClickOutside);
        }
    };
    
    // 알림 목록 로드
    window.loadNotifications = function() {
        const notificationList = document.getElementById('notificationList');
        if (!notificationList) return;
        
        // 로딩 표시
        notificationList.innerHTML = `
            <div class="notification-loading">
                <i class="bi bi-arrow-repeat spin"></i> 알림을 불러오는 중...
            </div>
        `;
        
        fetch('/api/notifications?page=0&size=5')
            .then(response => response.json())
            .then(notifications => {
                if (notifications.length === 0) {
                    notificationList.innerHTML = `
                        <div class="notification-empty">
                            <i class="bi bi-bell-slash"></i>
                            <p>알림이 없습니다.</p>
                        </div>
                    `;
                    
                    // 모두 읽음 버튼 숨기기
                    const markAllReadButton = document.getElementById('markAllReadButton');
                    if (markAllReadButton) {
                        markAllReadButton.style.display = 'none';
                    }
                } else {
                    let notificationHtml = '';
                    let hasUnread = false;
                    
                    notifications.forEach(notification => {
                        const isUnread = !notification.read;
                        if (isUnread) hasUnread = true;
                        
                        const date = new Date(notification.createdAt);
                        const formattedDate = window.formatNotificationTime(date);
                        
                        notificationHtml += `
                            <div class="notification-item ${isUnread ? 'notification-item-unread' : ''}" 
                                 data-id="${notification.notificationId}"
                                 data-url="${notification.url || '#'}"
                                 onclick="handleNotificationClick(this)">
                                <div class="notification-item-content">${notification.content}</div>
                                <div class="notification-item-time">${formattedDate}</div>
                            </div>
                        `;
                    });
                    
                    notificationList.innerHTML = notificationHtml;
                    
                    // 읽지 않은 알림이 없으면 버튼 숨기기
                    const markAllReadButton = document.getElementById('markAllReadButton');
                    if (markAllReadButton) {
                        markAllReadButton.style.display = hasUnread ? 'block' : 'none';
                    }
                }
            })
            .catch(error => {
                console.error('알림 목록 로드 실패:', error);
                notificationList.innerHTML = `
                    <div class="notification-empty">
                        <i class="bi bi-exclamation-triangle"></i>
                        <p>알림을 불러오는 중 오류가 발생했습니다.</p>
                    </div>
                `;
            });
    };
    
    // 알림 클릭 처리
    window.handleNotificationClick = function(element) {
        const notificationId = element.dataset.id;
        const url = element.dataset.url;
        
        // CSRF 토큰 가져오기
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
        
        const headers = {
            'Content-Type': 'application/json'
        };
        
        if (csrfToken && csrfHeader) {
            headers[csrfHeader] = csrfToken;
        }
        
        // 알림 읽음 처리
        fetch(`/api/notifications/${notificationId}/read`, {
            method: 'POST',
            headers: headers
        })
        .then(response => {
            if (response.ok) {
                // 읽음 처리 성공 시 스타일 변경
                element.classList.remove('notification-item-unread');
                
                // 읽지 않은 알림 개수 업데이트
                if (typeof window.updateNotificationCount === 'function') {
                    window.updateNotificationCount();
                }
                
                // 페이지 이동
                if (url && url !== '#') {
                    window.location.href = url;
                }
            }
        })
        .catch(error => {
            console.error('알림 읽음 처리 실패:', error);
        });
    };
    
    // 모두 읽음 처리
    window.markAllAsRead = function() {
        // CSRF 토큰 가져오기
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
        
        const headers = {
            'Content-Type': 'application/json'
        };
        
        if (csrfToken && csrfHeader) {
            headers[csrfHeader] = csrfToken;
        }
        
        fetch('/api/notifications/read-all', {
            method: 'POST',
            headers: headers
        })
        .then(response => {
            if (response.ok) {
                // 모든 알림 읽음 스타일 적용
                document.querySelectorAll('.notification-item-unread').forEach(item => {
                    item.classList.remove('notification-item-unread');
                });
                
                // 모두 읽음 버튼 숨기기
                const markAllReadButton = document.getElementById('markAllReadButton');
                if (markAllReadButton) {
                    markAllReadButton.style.display = 'none';
                }
                
                // 알림 개수 업데이트
                if (typeof window.updateNotificationCount === 'function') {
                    window.updateNotificationCount();
                }
            }
        })
        .catch(error => {
            console.error('모든 알림 읽음 처리 실패:', error);
        });
    };
    
    // 알림 시간 포맷팅
    window.formatNotificationTime = function(date) {
        const now = new Date();
        const diff = Math.floor((now - date) / 1000); // 초 단위 차이
        
        if (diff < 60) {
            return '방금 전';
        } else if (diff < 3600) {
            return Math.floor(diff / 60) + '분 전';
        } else if (diff < 86400) {
            return Math.floor(diff / 3600) + '시간 전';
        } else if (diff < 604800) { // 7일
            return Math.floor(diff / 86400) + '일 전';
        } else {
            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const day = String(date.getDate()).padStart(2, '0');
            return `${year}-${month}-${day}`;
        }
    };
}
