package com.example.demo.domain.chat.config;//package com.example.chatserver.chat.config;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.socket.config.annotation.EnableWebSocket;
//import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
//import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
//
//@Configuration
//@EnableWebSocket
//public class WebSocketConfig implements WebSocketConfigurer {
//
//    private final SimpleWebsocketHandler simpleWebsocketHandler;
//
//    public WebSocketConfig(SimpleWebsocketHandler simpleWebsocketHandler) {
//        this.simpleWebsocketHandler = simpleWebsocketHandler;
//    }
//
//    @Override
//    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
//        //웹소켓 소스코드를 처리할 웹소켓 핸들러 등록하는 메서드
//
//        // /connect url로 websocket 연결 요청이 들어오면, 핸들러 클래스가 처리
//        registry.addHandler(simpleWebsocketHandler, "/connect")
//                //securityconfig에서의 cors 예외는 http 요청에 대한 예외.
//                //따라서 웹소켓 프로토콜에 대한 용청에 대해서는 별도의 cors 설정 필요.
//                .setAllowedOrigins("http://localhost:3000");
//
//    }
//}
