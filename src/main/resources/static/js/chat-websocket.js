/**
 * 채팅 웹소켓 관련 기능
 */
(function() {
    'use strict';

    let stompClient = null;
    let isConnected = false;
    let connectAttempts = 0;
    let connectTimeout = null;
    let subscribedRooms = {};
    
    // 상태 표시 요소
    let connectionStatusEl = null;
    
    // 웹소켓 연결
    function connect(userEmail, onConnect, onError) {
        // 이미 연결된 경우 처리
        if (isConnected && stompClient) {
            console.log('이미 웹소켓에 연결되어 있습니다.');
            if (typeof onConnect === 'function') {
                onConnect(stompClient);
            }
            return;
        }
        
        console.log('웹소켓 연결 시도...');
        
        // 연결 상태 표시 업데이트
        updateConnectionStatus('연결 중...');
        
        // 재시도 카운터 증가
        connectAttempts++;
        
        // 연결 타임아웃 설정 (15초)
        if (connectTimeout) {
            clearTimeout(connectTimeout);
        }
        
        connectTimeout = setTimeout(() => {
            if (!isConnected) {
                console.error('웹소켓 연결 타임아웃');
                updateConnectionStatus('연결 실패. 재시도 중...');
                
                // 재시도 로직
                if (connectAttempts < 3) {
                    setTimeout(() => connect(userEmail, onConnect, onError), 3000);
                } else {
                    updateConnectionStatus('연결 실패. 페이지를 새로고침 해주세요.');
                    if (typeof onError === 'function') {
                        onError(new Error('연결 타임아웃'));
                    }
                }
            }
        }, 15000);
        
        // STOMP 클라이언트 생성
        const socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);
        
        // 디버그 로깅 설정
        Stomp.debug = function(str) {
            console.debug('STOMP: ' + str);
        };
        
        // CSRF 토큰 가져오기
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content') || '';
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') || 'X-CSRF-TOKEN';

        // 연결 헤더 설정
        const headers = {};
        headers[csrfHeader] = csrfToken;
        headers['username'] = userEmail;  // 명시적으로 사용자 이메일 전달
        
        // 연결 시도
        stompClient.connect(
            headers,
            // 연결 성공 콜백
            function(frame) {
                clearTimeout(connectTimeout);
                isConnected = true;
                console.log('웹소켓 연결 성공:', frame);
                updateConnectionStatus('연결됨', true);
                
                // 개인 알림 구독 (읽지 않은 메시지 업데이트)
                stompClient.subscribe('/queue/user.' + userEmail + '.unread', function(message) {
                    try {
                        console.log('읽지 않은 메시지 업데이트 수신:', message);
                        const event = new CustomEvent('unreadMessagesUpdate', {
                            detail: { rooms: JSON.parse(message.body) }
                        });
                        document.dispatchEvent(event);
                    } catch (e) {
                        console.error('알림 처리 오류:', e);
                    }
                });
                
                // 연결 성공 콜백 호출
                if (typeof onConnect === 'function') {
                    onConnect(stompClient);
                }
            },
            // 연결 실패 콜백
            function(error) {
                clearTimeout(connectTimeout);
                isConnected = false;
                console.error('웹소켓 연결 실패:', error);
                updateConnectionStatus('연결 실패. 재시도 중...');
                
                // 재시도 로직
                if (connectAttempts < 3) {
                    setTimeout(() => connect(userEmail, onConnect, onError), 3000);
                } else {
                    updateConnectionStatus('연결 실패. 페이지를 새로고침 해주세요.');
                    if (typeof onError === 'function') {
                        onError(error);
                    }
                }
            }
        );
    }
    
    // 연결 상태 업데이트
    function updateConnectionStatus(text, isConnected) {
        if (!connectionStatusEl) {
            connectionStatusEl = document.getElementById('connectionStatus');
        }
        
        if (connectionStatusEl) {
            connectionStatusEl.textContent = text;
            
            if (isConnected) {
                connectionStatusEl.className = 'connection-status connected';
            } else {
                connectionStatusEl.className = 'connection-status disconnected';
            }
        }
    }
    
    // 연결 종료
    function disconnect() {
        if (stompClient && stompClient.connected) {
            stompClient.disconnect();
            isConnected = false;
            subscribedRooms = {};
            updateConnectionStatus('연결 끊김');
            console.log('웹소켓 연결 종료');
        }
    }
    
    // 채팅방 구독
    function subscribeToRoom(roomId, onMessageReceived) {
        if (!stompClient || !stompClient.connected) {
            console.error('웹소켓이 연결되지 않았습니다.');
            return false;
        }
        
        if (subscribedRooms[roomId]) {
            console.log(`이미 채팅방 ${roomId}에 구독 중입니다.`);
            return true;
        }
        
        try {
            console.log(`채팅방 ${roomId} 구독 시도`);
            
            // 채팅방 메시지 구독
            const subscription = stompClient.subscribe('/topic/chat.room.' + roomId, function(message) {
                console.log('채팅 메시지 수신:', message);
                try {
                    const messageData = JSON.parse(message.body);
                    
                    // 메시지 수신 콜백
                    if (typeof onMessageReceived === 'function') {
                        onMessageReceived(messageData);
                    }
                } catch (e) {
                    console.error('메시지 처리 오류:', e);
                }
            });
            
            // 구독 정보 저장
            subscribedRooms[roomId] = subscription;
            console.log(`채팅방 ${roomId} 구독 성공`);
            
            return true;
        } catch (e) {
            console.error(`채팅방 ${roomId} 구독 실패:`, e);
            return false;
        }
    }
    
    // 채팅방 구독 해제
    function unsubscribeFromRoom(roomId) {
        if (subscribedRooms[roomId]) {
            try {
                subscribedRooms[roomId].unsubscribe();
                delete subscribedRooms[roomId];
                console.log(`채팅방 ${roomId} 구독 해제 성공`);
                return true;
            } catch (e) {
                console.error(`채팅방 ${roomId} 구독 해제 실패:`, e);
                return false;
            }
        }
        return false;
    }
    
    // 입장 메시지 전송
    function sendEnterMessage(roomId, userEmail) {
        if (!stompClient || !stompClient.connected) {
            console.error('웹소켓이 연결되지 않았습니다.');
            return false;
        }
        
        try {
            console.log('채팅방 입장 메시지 전송:', roomId);
            stompClient.send("/app/chat/enter", {'username': userEmail}, JSON.stringify({
                roomId: roomId,
                senderEmail: userEmail,
                message: "입장했습니다."
            }));
            return true;
        } catch (e) {
            console.error('입장 메시지 전송 실패:', e);
            return false;
        }
    }
    
    // 채팅 메시지 전송
    function sendChatMessage(roomId, userEmail, message) {
        if (!stompClient || !stompClient.connected) {
            console.error('웹소켓이 연결되지 않았습니다.');
            return false;
        }
        
        if (!message || !message.trim()) {
            console.warn('메시지가 비어있습니다.');
            return false;
        }
        
        try {
            const chatMessage = {
                roomId: roomId,
                senderEmail: userEmail,
                message: message
            };
            
            stompClient.send("/app/chat/message", {'username': userEmail}, JSON.stringify(chatMessage));
            return true;
        } catch (e) {
            console.error('메시지 전송 실패:', e);
            return false;
        }
    }
    
    // 연결 상태 확인
    function isConnectedToWebSocket() {
        return isConnected && stompClient && stompClient.connected;
    }
    
    // 전역으로 노출
    window.ChatWebSocket = {
        connect: connect,
        disconnect: disconnect,
        subscribeToRoom: subscribeToRoom,
        unsubscribeFromRoom: unsubscribeFromRoom,
        sendEnterMessage: sendEnterMessage,
        sendChatMessage: sendChatMessage,
        isConnected: isConnectedToWebSocket
    };
})();
