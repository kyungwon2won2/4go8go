package com.example.demo.domain.chat.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

//이벤트가 발생했구나를 그냥 캐치해서 정보를 받아오는 로그 찍는 용도

//스프링과 stomp는 기본적으로 세션관리를 자동으로 내부적으로 처리.
//연결-해제 이벤트를 기록, 연결된 세션수를 실시간으로 확인할 목적으로 이벤트 리스너를 생성 => 로그, 디버깅 목적

@Slf4j
@Component
public class StompEventListener {

    private final Set<String> sessions = ConcurrentHashMap.newKeySet();

    @EventListener
    public void connectHandle(SessionConnectEvent event){
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        sessions.add(accessor.getSessionId());
        log.info("connect session ID: " + accessor.getSessionId());
        log.info("total session: " + sessions.size());
    }

    @EventListener
    public void disconnectHandle(SessionDisconnectEvent event){
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        sessions.add(accessor.getSessionId());
        log.info("disconnect session ID: " + accessor.getSessionId());
        log.info("total session: " + sessions.size());
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        if (sha.getCommand() == StompCommand.CONNECT) {
            Authentication auth = (Authentication) sha.getUser();
            if (auth != null && auth.isAuthenticated()) {
                System.out.println("웹소켓 접속한 사용자: " + auth.getName());
                // auth.getPrincipal()을 캐스팅해서 이메일 등도 출력 가능
            } else {
                System.out.println("웹소켓 접속 사용자 인증 안됨");
            }
        }
    }
}
