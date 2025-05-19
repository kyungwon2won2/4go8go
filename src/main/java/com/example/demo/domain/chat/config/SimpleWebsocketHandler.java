package com.example.demo.domain.chat.config;//package com.example.chatserver.chat.config;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.*;
//import org.springframework.web.socket.handler.TextWebSocketHandler;
//
//import java.util.Set;
//import java.util.concurrent.ConcurrentHashMap;
//
////connect로 웹소켓 연결요청이 들어왔을때 이를 처리할 클래스
///*
//    생각해야할것.
//    - /connect를 하면 서버가 연결 유저 정보를 가지고있고, 메세지를 쏴줘야함.
//    - /connect는 http 요청이 아니다. 그렇지만 filter를 거쳐야하기 때문에...
//        1. token filter를 제외시켜줘야함.
//        2. cors는 http 요청 대상이기 때문에 웹소켓 cors 설정을 별도로 해줘야함.
// */
//@Slf4j
//@Component
//public class SimpleWebsocketHandler extends TextWebSocketHandler {
//
//    //연결 세션 관리 : 쓰레드 safe한 자료구조(동시에 여러 사용자가 들어와도 문제x)
//    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
//
//    //접속되면 그후에...
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//        //접속하면 세션을 담아준다.
//        sessions.add(session);
//        log.info("Connected : {}", session.getId());
//    }
//
//    //메시지가 들어왔을때...
//    @Override
//    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
//        String payload = message.getPayload();
//        log.info("received message : {}", payload);
//        for(WebSocketSession s: sessions){
//            if(s.isOpen()){
//                s.sendMessage(new TextMessage(payload));
//            }
//        }
//    }
//
//    //연결이 종료되면 그후에...
//    @Override
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
//        sessions.remove(session);
//        log.info("Disconnected : {}", session.getId());
//    }
//}
