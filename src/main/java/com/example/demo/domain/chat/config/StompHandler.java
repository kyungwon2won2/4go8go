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

@Slf4j
@Component
public class StompHandler implements ChannelInterceptor {

    private final ChatService chatService;

    public StompHandler(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if(StompCommand.CONNECT == accessor.getCommand()){
            log.info("CONNECT 요청: 세션 유효성 검증");
            // 세션 기반 인증 확인
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated() || 
                authentication.getPrincipal().equals("anonymousUser")) {
                throw new AuthenticationServiceException("인증되지 않은 사용자입니다.");
            }
            log.info("세션 검증 완료: {}", authentication.getName());
        }
        
        if(StompCommand.SUBSCRIBE == accessor.getCommand()){
            log.info("SUBSCRIBE 요청: 채팅방 접근 권한 검증");
            
            // 세션에서 현재 인증된 사용자 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated() || 
                authentication.getPrincipal().equals("anonymousUser")) {
                throw new AuthenticationServiceException("인증되지 않은 사용자입니다.");
            }
            
            String email = authentication.getName();
            String roomId = accessor.getDestination().split("/")[2];
            
            if(!chatService.isRoomParticipant(email, Long.parseLong(roomId))){
                throw new AuthenticationServiceException("해당 채팅방에 접근 권한이 없습니다.");
            }
        }

        return message;
    }
}
