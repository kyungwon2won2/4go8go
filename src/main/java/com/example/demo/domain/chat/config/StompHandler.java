package com.example.demo.domain.chat.config;

import com.example.demo.domain.chat.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class StompHandler implements ChannelInterceptor {

    private final ChatService chatService;

    // 현재 활성화된 세션을 추적하기 위한 맵
    private final Map<String, String> activeSessionMap = new ConcurrentHashMap<>();

    public StompHandler(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        try {
            final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
            final StompCommand command = accessor.getCommand();

            // 명령이 null인 경우 (특히 연결 종료 후)는 그대로 처리
            if (command == null) {
                return message;
            }

            try {
                if (StompCommand.CONNECT == command) {
                    handleConnect(accessor);
                }
                else if (StompCommand.SUBSCRIBE == command) {
                    handleSubscribe(accessor);
                }
                else if (StompCommand.SEND == command) {
                    handleSend(accessor);
                }
                else if (StompCommand.DISCONNECT == command) {
                    handleDisconnect(accessor);
                }
            } catch (Exception e) {
                log.error("메시지 처리 중 오류 발생", e);
            }
        } catch (Exception e) {
            log.error("StompHandler 처리 중 오류 발생", e);
        }

        return message;
    }

    // CONNECT 명령 처리
    private void handleConnect(StompHeaderAccessor accessor) {
        // 세션 기반 인증 확인
        Authentication authentication = getAuthentication(accessor);

        // 클라이언트에서 전달한 username 확인
        String username = accessor.getFirstNativeHeader("username");

        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getPrincipal().equals("anonymousUser")) {

            // 연결 성공 및 세션 추적
            String sessionId = accessor.getSessionId();
            String userEmail = authentication.getName();

            if (sessionId != null) {
                activeSessionMap.put(sessionId, userEmail);
            }
        } else if (username != null && !username.isEmpty()) {
            // 인증 정보는 없지만 username 헤더가 있는 경우
            String sessionId = accessor.getSessionId();
            if (sessionId != null) {
                activeSessionMap.put(sessionId, username);
            }
        }
    }

    // SUBSCRIBE 명령 처리
    private void handleSubscribe(StompHeaderAccessor accessor) {
        // 인증 확인
        Authentication authentication = getAuthentication(accessor);
        String username = accessor.getFirstNativeHeader("username");
        String sessionId = accessor.getSessionId();
        String email = null;

        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getPrincipal().equals("anonymousUser")) {
            email = authentication.getName();
        } else if (username != null && !username.isEmpty()) {
            // 헤더에서 사용자 이름 사용
            email = username;
        } else if (sessionId != null && activeSessionMap.containsKey(sessionId)) {
            // 세션 ID로 사용자 정보 찾기
            email = activeSessionMap.get(sessionId);
        }

        String destination = accessor.getDestination();

        // destination이 null인 경우 처리
        if (destination == null) {
            return;
        }

        // 채팅방 구독인 경우만 확인 - RabbitMQ용 경로
        if (destination.startsWith("/topic/chat.room.")) {
            String[] parts = destination.split("\\.");
            if (parts.length < 3) {
                return;
            }

            String roomId = parts[2];  // chat.room.{roomId}에서 roomId 추출

            try {
                Long roomIdLong = Long.parseLong(roomId);

                // 현재는 권한 체크 결과에 상관없이 구독을 허용
                if (email != null) {
                    chatService.isRoomParticipant(email, roomIdLong);
                }
            } catch (NumberFormatException e) {
                // 오류 무시
            }
        }
    }

    // SEND 명령 처리
    private void handleSend(StompHeaderAccessor accessor) {
        // 메시지 처리만 수행, 로깅 제거
    }

    // DISCONNECT 명령 처리
    private void handleDisconnect(StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();

        if (sessionId != null && activeSessionMap.containsKey(sessionId)) {
            activeSessionMap.remove(sessionId);
        }
    }

    // Authentication 객체 얻기 (SecurityContext에서)
    private Authentication getAuthentication(StompHeaderAccessor accessor) {
        // 1. SecurityContext에서 Authentication 객체 확인
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return authentication;
    }

    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        // 로그 제거
    }
}
