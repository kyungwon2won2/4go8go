/**
 * 알림 SSE(Server-Sent Events) 연결 및 실시간 알림 처리
 */
(function () {
    'use strict';

    // SSE 연결 객체
    let eventSource = null;

    // 마지막 연결 시도 시간
    let lastConnectionAttempt = 0;

    // 최소 재연결 간격 (밀리초)
    const MIN_RECONNECT_INTERVAL = 10000; // 10초

    // 초기화
    document.addEventListener('DOMContentLoaded', function () {
        // 인증된 사용자인지 확인 (더 정확한 방법)
        const isAuthenticated = checkUserAuthentication();

        if (isAuthenticated) {
            // 브라우저 알림 권한 요청
            if (window.NotificationUtils) {
                window.NotificationUtils.requestNotificationPermission();
            }

            // 알림 개수 초기 로드
            if (window.NotificationUtils) {
                window.NotificationUtils.loadNotificationCount();
            }

            // SSE 연결 설정
            connectToSSE();

            // 연결 상태 모니터링 (디버깅용)
            startConnectionMonitoring();

            // 브라우저 가시성 변경 시 연결 관리
            document.addEventListener('visibilitychange', function () {
                if (document.visibilityState === 'hidden') {
                    // 페이지가 백그라운드에 있을 때 연결 종료
                    if (eventSource) {
                        console.log('페이지 백그라운드: SSE 연결 일시 중지');
                        eventSource.close();
                        eventSource = null;
                    }
                } else if (document.visibilityState === 'visible') {
                    // 페이지가 다시 보이면 연결 재개 (인증 상태 재확인)
                    if (checkUserAuthentication()) {
                        console.log('페이지 포그라운드: SSE 연결 재개');
                        connectToSSE();
                    }
                }
            });
        } else {
            console.log('비로그인 사용자 - SSE 연결하지 않음');
        }
    });

    // 사용자 인증 상태 확인
    function checkUserAuthentication() {
        // 1. body 태그의 data-authenticated 속성 확인
        const isAuthenticated = document.body.hasAttribute('data-authenticated') &&
            document.body.getAttribute('data-authenticated') === 'true';

        // 2. 추가로 헤더의 로그아웃 버튼 존재 여부로 확인
        const logoutForm = document.querySelector('form[action="/logout"]');
        const hasLogoutButton = !!logoutForm;

        console.log('인증 상태 확인 - data-authenticated:', isAuthenticated, 'logoutButton:', hasLogoutButton);

        return isAuthenticated || hasLogoutButton;
    }

    // 연결 상태 모니터링 (디버깅용)
    function startConnectionMonitoring() {
        setInterval(function () {
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
        // 연결 전 인증 상태 재확인
        if (!checkUserAuthentication()) {
            console.log('인증되지 않은 사용자 - SSE 연결 중단');
            return;
        }

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

        // 전역에서 접근 가능하도록 설정
        window.eventSource = eventSource;

        // 연결 이벤트
        eventSource.addEventListener('connect', function (event) {
            console.log('SSE 연결 성공:', event.data);
        });

        // 서버 에러 이벤트 (인증 실패 등)
        eventSource.addEventListener('error', function (event) {
            console.log('SSE 서버 에러 수신:', event.data);
            // 인증 오류인 경우 재연결하지 않음
            if (event.data && event.data.includes('인증')) {
                console.log('인증 오류로 인한 SSE 연결 종료');
                if (eventSource) {
                    eventSource.close();
                    eventSource = null;
                    window.eventSource = null;
                }
                return;
            }
        });

        // 핑 이벤트 (연결 유지용)
        eventSource.addEventListener('ping', function (event) {
            console.log('SSE 핑 수신:', event.data);
        });

        // 채팅 메시지 알림
        eventSource.addEventListener('CHAT_MESSAGE', function (event) {
            try {
                console.log('채팅 메시지 알림 수신:', event.data);
                const notification = JSON.parse(event.data);
                handleNotification(notification);
            } catch (e) {
                console.error('알림 처리 오류:', e);
            }
        });

        // 채팅 아이콘 업데이트 이벤트
        eventSource.addEventListener('CHAT_MESSAGE_ICON_UPDATE', function (event) {
            try {
                console.log('채팅 아이콘 업데이트 알림 수신:', event.data);
                const notification = JSON.parse(event.data);

                // 로그인 상태 확인 후 채팅 아이콘 업데이트
                if (window.NotificationChat && typeof window.NotificationChat.updateChatIcon === 'function') {
                    // 인증 상태 확인
                    if (window.NotificationChat.isUserAuthenticated && window.NotificationChat.isUserAuthenticated()) {
                        window.NotificationChat.updateChatIcon(true);

                        // 브라우저 알림 표시
                        if (window.NotificationUtils) {
                            window.NotificationUtils.showBrowserNotification(notification);
                        }
                    } else {
                        console.log('비로그인 사용자 - 채팅 알림 무시');
                    }
                }
            } catch (e) {
                console.error('채팅 아이콘 업데이트 오류:', e);
            }
        });

        // 댓글 작성 알림
        eventSource.addEventListener('POST_COMMENT', function (event) {
            try {
                console.log('댓글 알림 수신:', event.data);
                const notification = JSON.parse(event.data);
                handleNotification(notification);
            } catch (e) {
                console.error('알림 처리 오류:', e);
            }
        });

        // 일반 메시지
        eventSource.onmessage = function (event) {
            console.log('SSE 메시지 수신:', event.data);
        };

        // 에러 처리
        eventSource.onerror = function (error) {
            console.error('SSE 연결 오류:', error);

            // 연결이 닫혔을 때만 재연결 시도
            if (eventSource.readyState === EventSource.CLOSED) {
                console.log('SSE 연결이 닫혔습니다. 재연결 시도 예약...');
                eventSource = null;
                window.eventSource = null;

                // 인증 상태 확인 후 재연결
                if (checkUserAuthentication()) {
                    // 일정 시간 후 재연결 시도
                    setTimeout(function () {
                        console.log('SSE 재연결 시도 중...');
                        connectToSSE();
                    }, MIN_RECONNECT_INTERVAL);
                } else {
                    console.log('비로그인 상태 - 재연결하지 않음');
                }
            }
        };

        // 페이지 언로드 시 연결 종료
        window.addEventListener('beforeunload', function () {
            if (eventSource) {
                console.log('페이지 언로드: SSE 연결 종료');
                eventSource.close();
                eventSource = null;
                window.eventSource = null;
            }
        });
    }

    // 알림 처리 함수
    function handleNotification(notification) {
        console.log('새 알림 처리:', notification.type);

        // 브라우저 알림 표시
        if (window.NotificationUtils) {
            window.NotificationUtils.showBrowserNotification(notification);
        }

        // 알림 개수 업데이트
        if (window.NotificationUtils) {
            window.NotificationUtils.loadNotificationCount();
        }

        // 알림 드롭다운이 열려있는 경우 목록 갱신
        if (window.NotificationUI && typeof window.NotificationUI.loadNotifications === 'function') {
            const notificationDropdown = document.getElementById('notificationDropdown');
            if (notificationDropdown && notificationDropdown.style.display === 'block') {
                console.log('알림 드롭다운이 열려있어 목록 갱신');
                window.NotificationUI.loadNotifications();
            }
        }
    }
})();