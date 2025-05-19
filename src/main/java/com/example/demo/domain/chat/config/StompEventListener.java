package com.example.demo.domain.chat.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class StompEventListener {

    private final Set<String> sessions = ConcurrentHashMap.newKeySet();

    @EventListener
    public void connectHandle(SessionConnectEvent event){
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        if (accessor.getSessionId() != null) {
            sessions.add(accessor.getSessionId());
        }
    }

    @EventListener
    public void disconnectHandle(SessionDisconnectEvent event){
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        if (accessor.getSessionId() != null) {
            sessions.remove(accessor.getSessionId());
        }
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        // 로그 제거
    }
}