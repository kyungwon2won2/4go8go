(function() {
    'use strict';
    
    // SSE 연결 객체
    let eventSource = null;
    
    // 마지막 연결 시도 시간
    let lastConnectionAttempt = 0;
    
    // 최소 재연결 간격 (밀리초)
    const MIN_RECONNECT_INTERVAL = 10000; // 10초
    
    // 초기화
    document.addEventListener('DOMContentLoaded', function() {
        // 인증된 사용자인지 확인 (body 태그에 data-authenticated 속성이 있어야 함)
        const isAuthenticated = document.body.hasAttribute('data-authenticated') && 
                               document.body.getAttribute('data-authenticated') === 'true';
        
        if (isAuthenticated) {
            // SSE 연결 설정
            connectToSSE();
            
            // 브라우저 알림 권한 요청
            requestNotificationPermission();
            
            // 알림 개수 초기 로드
            loadNotificationCount();
            
            // 연결 상태 로깅 (디버깅용)
            startConnectionMonitoring();
            
            // 브라우저 가시성 변경 시 연결 관리
            document.addEventListener('visibilitychange', function() {
                if (document.visibilityState === 'hidden') {
                    // 페이지가 백그라운드에 있을 때 연결 종료
                    if (eventSource) {
                        console.log('페이지 백그라운드: SSE 연결 일시 중지');
                        eventSource.close();
                        eventSource = null;
                    }
                } else if (document.visibilityState === 'visible') {
                    // 페이지가 다시 보이면 연결 재개
                    console.log('페이지 포그라운드: SSE 연결 재개');
                    connectToSSE();
                }
            });
        }
    });
    
    // 연결 상태 모니터링 (디버깅용)
    function startConnectionMonitoring() {
        setInterval(function() {
            if (eventSource) {
                const stateText = 
                    eventSource.readyState === 0 ? '연결 중' :
                    eventSource.readyState === 1 ? '열림' :
                    '닫힘';
                    
                console.log(`SSE 연결 상태: ${stateText} (readyState: ${eventSource.readyState})`);
            } else {
                console.log('SSE 연결이 없음');
            }
        }, 30000); // 30초마다 확인
    }
    
    // SSE 연결 설정
    function connectToSSE() {
        if (typeof EventSource === 'undefined') {
            console.error('이 브라우저는 Server-Sent Events를 지원하지 않습니다.');
            return;
        }
        
        // 이미 활성화된 연결이 있는지 확인
        if (eventSource && eventSource.readyState === EventSource.OPEN) {
            console.log('SSE 연결이 이미 활성화되어 있습니다.');
            return;
        }
        
        // 연결 중인지 확인
        if (eventSource && eventSource.readyState === EventSource.CONNECTING) {
            console.log('SSE 연결 중입니다. 기다려주세요.');
            return;
        }
        
        // 최소 재연결 간격 확인
        const now = Date.now();
        if (now - lastConnectionAttempt < MIN_RECONNECT_INTERVAL) {
            console.log('SSE 재연결 간격이 너무 짧습니다. 잠시 후 다시 시도합니다.');
            setTimeout(connectToSSE, MIN_RECONNECT_INTERVAL - (now - lastConnectionAttempt));
            return;
        }
        
        // 마지막 연결 시도 시간 업데이트
        lastConnectionAttempt = now;
        
        // 기존 연결 정리
        if (eventSource) {
            console.log('기존 SSE 연결 종료');
            eventSource.close();
            eventSource = null;
        }
        
        console.log('새 SSE 연결 시도...');
        
        // 캐시 방지를 위한 타임스탬프 추가
        eventSource = new EventSource(`/api/sse/subscribe?t=${now}`);
        
        // 연결 이벤트
        eventSource.addEventListener('connect', function(event) {
            console.log('SSE 연결 성공:', event.data);
        });
        
        // 핑 이벤트 (연결 유지용)
        eventSource.addEventListener('ping', function(event) {
            console.log('SSE 핑 수신:', event.data);
        });
        
        // 채팅 메시지 알림
        eventSource.addEventListener('CHAT_MESSAGE', function(event) {
            try {
                const notification = JSON.parse(event.data);
                handleNotification(notification);
            } catch (e) {
                console.error('알림 처리 오류:', e);
            }
        });
        
        // 채팅 아이콘 업데이트 이벤트
        eventSource.addEventListener('CHAT_MESSAGE_ICON_UPDATE', function(event) {
            try {
                console.log('채팅 메시지 알림 수신:', event.data);
                const notification = JSON.parse(event.data);
                
                // 채팅 아이콘 업데이트 및 뱃지 표시
                // 직접 DOM 요소에 접근
                const chatIcon = document.getElementById('chatMessageIcon');
                const chatBadge = document.getElementById('chatMessageBadge');
                
                if (chatIcon) {
                    chatIcon.classList.add('has-new');
                }
                
                if (chatBadge) {
                    chatBadge.style.display = 'block';
                }
                
                // 외부 함수 사용 (있는 경우)
                if (typeof window.updateChatIcon === 'function') {
                    window.updateChatIcon(true);
                }
                
                // 상태 저장 (페이지 전환 시 유지)
                try {
                    localStorage.setItem('hasChatMessage', 'true');
                } catch (e) {
                    console.warn('채팅 상태 저장 실패:', e);
                }
                
                // 브라우저 알림 표시
                showBrowserNotification(notification);
            } catch (e) {
                console.error('채팅 아이콘 업데이트 오류:', e);
            }
        });

        // 댓글 작성 알림
        eventSource.addEventListener('POST_COMMENT', function(event) {
            try {
                const notification = JSON.parse(event.data);
                handleNotification(notification);
            } catch (e) {
                console.error('알림 처리 오류:', e);
            }
        });

        // 댓글 작성 아이콘 업데이트 이벤트

        
        // 일반 메시지
        eventSource.onmessage = function(event) {
            console.log('SSE 메시지 수신:', event.data);
        };
        
        // 에러 처리
        eventSource.onerror = function(error) {
            console.error('SSE 연결 오류:', error);
            
            // 연결이 닫혔을 때만 재연결 시도
            if (eventSource.readyState === EventSource.CLOSED) {
                console.log('SSE 연결이 닫혔습니다. 재연결 시도 예약...');
                eventSource = null;
                
                // 일정 시간 후 재연결 시도
                setTimeout(function() {
                    console.log('SSE 재연결 시도 중...');
                    connectToSSE();
                }, MIN_RECONNECT_INTERVAL);
            }
        };
        
        // 페이지 언로드 시 연결 종료
        window.addEventListener('beforeunload', function() {
            if (eventSource) {
                console.log('페이지 언로드: SSE 연결 종료');
                eventSource.close();
                eventSource = null;
            }
        });
    }
    
    // 알림 처리
    function handleNotification(notification) {
        // 브라우저 알림 표시
        showBrowserNotification(notification);
        
        // 알림 뱃지 업데이트
        loadNotificationCount();
        
        // 알림 아이콘 색상 변경
        updateNotificationIcon(true);
        
        // 알림 모달이 열려있는 경우 목록 갱신
        if (window.notificationModalOpen && typeof window.loadNotifications === 'function') {
            window.loadNotifications();
        }
    }
    
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
    
    // 채팅 아이콘 업데이트
    function updateChatIcon(hasNew) {
        const chatIcon = document.getElementById('chatMessageIcon');
        if (chatIcon) {
            if (hasNew) {
                chatIcon.classList.add('has-new');
                
                // 채팅 뱃지 표시
                const chatBadge = document.getElementById('chatMessageBadge');
                if (chatBadge) {
                    chatBadge.style.display = 'block';
                }
            } else {
                chatIcon.classList.remove('has-new');
                
                // 채팅 뱃지 숨기기
                const chatBadge = document.getElementById('chatMessageBadge');
                if (chatBadge) {
                    chatBadge.style.display = 'none';
                }
            }
        }
    }
    
    // 브라우저 알림 표시
    function showBrowserNotification(notification) {
        if (Notification.permission === 'granted') {
            const browserNotification = new Notification('4고8고마켓 알림', {
                body: notification.content,
                icon: '/images/4go8go_logo.png'
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
            Notification.requestPermission();
        }
    }
    
    // 알림 개수 로드
    function loadNotificationCount() {
        fetch('/api/notifications/count')
            .then(response => response.json())
            .then(data => {
                updateNotificationBadge(data.count);
                
                // 읽지 않은 알림이 있으면 아이콘 색상 변경
                updateNotificationIcon(data.count > 0);
            })
            .catch(error => {
                console.error('알림 개수 로드 실패:', error);
            });
    }
    
    // 알림 뱃지 업데이트 (전역 함수로 노출하여 모달에서도 사용할 수 있게 함)
    function updateNotificationBadge(count) {
        const badge = document.getElementById('notificationBadge');
        if (badge) {
            if (count > 0) {
                badge.textContent = count > 99 ? '99+' : count;
                badge.style.display = 'block';
            } else {
                badge.style.display = 'none';
            }
        }
    }
    
    // 전역 함수로 노출 (헤더의 스크립트에서 사용)
    window.updateNotificationCount = function() {
        loadNotificationCount();
    };
    
    // 채팅 아이콘 업데이트 함수 전역으로 노출
    window.updateChatIcon = function(hasNew) {
        const chatIcon = document.getElementById('chatMessageIcon');
        if (chatIcon) {
            if (hasNew) {
                chatIcon.classList.add('has-new');
                
                // 채팅 뱃지 표시
                const chatBadge = document.getElementById('chatMessageBadge');
                if (chatBadge) {
                    chatBadge.style.display = 'block';
                }
            } else {
                chatIcon.classList.remove('has-new');
                
                // 채팅 뱃지 숨기기
                const chatBadge = document.getElementById('chatMessageBadge');
                if (chatBadge) {
                    chatBadge.style.display = 'none';
                }
            }
        }
    };
})();