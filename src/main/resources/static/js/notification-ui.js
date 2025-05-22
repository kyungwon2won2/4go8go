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
            fetch('/api/notifications/count')
                .then(response => response.json())
                .then(data => {
                    if (data.count > 0) {
                        notificationBadge.textContent = data.count > 99 ? '99+' : data.count;
                        notificationBadge.style.display = 'block';
                    }
                })
                .catch(error => {
                    console.error('알림 개수 로드 실패:', error);
                });
        }
    });
    
    // 이벤트 리스너 설정
    function setupEventListeners() {
        // 알림 아이콘 클릭 이벤트
        notificationIcon.addEventListener('click', function(e) {
            e.preventDefault();
            toggleDropdown();
            
            // 드롭다운이 열릴 때 알림 목록 로드
            if (!isDropdownVisible) {
                loadNotifications();
            }
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
    
    // 알림 목록 로드
    function loadNotifications() {
        fetch('/api/notifications?limit=5')  // 5개씩만 로드
            .then(response => response.json())
            .then(data => {
                notifications = data;
                updateNotificationList();
            })
            .catch(error => {
                console.error('알림 목록 로드 실패:', error);
            });
    }
    
    // 알림 목록 UI 업데이트
    function updateNotificationList() {
        if (!notificationList) return;
        
        // 기존 목록 비우기
        notificationList.innerHTML = '';
        
        if (notifications.length === 0) {
            notificationList.innerHTML = '<div class="empty-notification">새 알림이 없습니다.</div>';
            return;
        }
        
        // 알림 항목 추가
        notifications.forEach(notification => {
            const item = document.createElement('div');
            item.className = 'notification-item';
            if (!notification.isRead) {
                item.classList.add('unread');
            }
            
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
            time.textContent = formatTimestamp(notification.createdAt);
            
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
                // 읽음 처리
                if (!notification.isRead) {
                    markAsRead(notification.notificationId);
                }
                
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
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content') || '';
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') || 'X-CSRF-TOKEN';
        
        const headers = {
            'Content-Type': 'application/json'
        };
        
        // CSRF 토큰이 있는 경우에만 헤더에 추가
        if (csrfToken && csrfHeader) {
            headers[csrfHeader] = csrfToken;
        } else {
            console.warn('CSRF 토큰이 없습니다. 요청이 실패할 수 있습니다.');
        }
        
        fetch(`/api/notifications/${notificationId}`, {
            method: 'POST', // DELETE에서 POST로 변경
            headers: headers,
            credentials: 'same-origin' // 인증 정보 포함
        })
        .then(response => {
            if (!response.ok) {
                const contentType = response.headers.get('content-type');
                if (contentType && contentType.includes('application/json')) {
                    return response.json().then(data => {
                        throw new Error(data.message || '응답 오류: ' + response.status);
                    });
                } else {
                    throw new Error('응답 오류: ' + response.status);
                }
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
                }
                
                // 뱃지 업데이트
                updateNotificationBadge();
            } else {
                console.error('알림 삭제 실패:', data.message);
                alert('알림 삭제 중 오류가 발생했습니다.');
            }
        })
        .catch(error => {
            console.error('알림 삭제 실패:', error);
            alert('알림 삭제 중 오류가 발생했습니다. 다시 시도해주세요.');
        });
    }
    
    // 뱃지 업데이트 함수
    function updateNotificationBadge() {
        if (!notificationBadge) return;
        
        const unreadCount = notifications.filter(n => !n.isRead).length;
        if (unreadCount > 0) {
            notificationBadge.textContent = unreadCount > 99 ? '99+' : unreadCount;
            notificationBadge.style.display = 'block';
        } else {
            notificationBadge.style.display = 'none';
        }
    }
    
    // 알림 읽음 처리
    function markAsRead(notificationId) {
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content') || '';
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') || 'X-CSRF-TOKEN';
        
        const headers = {
            'Content-Type': 'application/json'
        };
        
        // CSRF 토큰이 있는 경우에만 헤더에 추가
        if (csrfToken && csrfHeader) {
            headers[csrfHeader] = csrfToken;
        } else {
            console.warn('CSRF 토큰이 없습니다. 요청이 실패할 수 있습니다.');
        }
        
        fetch(`/api/notifications/${notificationId}/read`, {
            method: 'POST', // PATCH에서 POST로 변경
            headers: headers,
            credentials: 'same-origin' // 인증 정보 포함
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('응답 오류: ' + response.status);
            }
            // 수정 - 응답이 비어있을 수 있으므로 조건부로 JSON 파싱
            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('application/json')) {
                return response.json();
            } else {
                return { status: response.ok ? 'success' : 'error' };
            }
        })
        .then(data => {
            // 로컬 목록에서도 업데이트
            const index = notifications.findIndex(n => n.notificationId === notificationId);
            if (index !== -1) {
                notifications[index].isRead = true;
                updateNotificationList();
                
                // 뱃지 업데이트
                updateNotificationBadge();
            }
        })
        .catch(error => {
            console.error('알림 읽음 처리 실패:', error);
        });
    }
    
    // 드롭다운 토글
    function toggleDropdown() {
        if (isDropdownVisible) {
            hideDropdown();
        } else {
            showDropdown();
        }
    }
    
    // 드롭다운 표시
    function showDropdown() {
        if (notificationDropdown) {
            notificationDropdown.style.display = 'block';
            isDropdownVisible = true;
        }
    }
    
    // 드롭다운 숨기기
    function hideDropdown() {
        if (notificationDropdown) {
            notificationDropdown.style.display = 'none';
            isDropdownVisible = false;
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
})();