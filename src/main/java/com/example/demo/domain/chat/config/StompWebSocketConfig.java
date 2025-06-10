package com.example.demo.domain.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

@Configuration
@EnableWebSocketMessageBroker //이러한 브로커가 들어가면 stomp다.
public class StompWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompHandler stompHandler;

    public StompWebSocketConfig(StompHandler stompHandler) {
        this.stompHandler = stompHandler;
    }


    //endpoint 설정, cors 설정
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .setHandshakeHandler(new DefaultHandshakeHandler())  // 세션 인증 유지
                //ws://가 아닌 http:// 엔드포인트를 사용할수 있게 해주는 sockJs 라이브러리를 통한 요청을 허용하는 설정.
                .withSockJS()
                .setSessionCookieNeeded(true); // 세션 쿠키를 전송하도록 설정
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        // 클라이언트에서 메시지를 보낼 때 사용할 prefix
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");


        //기본 stomp 내장 브로커 사용
        //registry.enableSimpleBroker("/sub", "/topic", "/queue", "/user");


        // RabbitMQ STOMP Broker Relay 설정 - RabbitMQ에 맞는 destination 패턴 사용
        registry.enableStompBrokerRelay("/topic", "/queue", "/exchange", "/amq/queue")
                .setRelayHost("localhost")
                .setRelayPort(61613)
                .setClientLogin("guest")
                .setClientPasscode("guest")
                .setSystemLogin("guest")
                .setSystemPasscode("guest");
    }

    //웹소켓 요청(connect, subscribe, disconnect)등의 요청시에는 http header등 http 메시지를 넣어올수 있고,
    //이를 interceptor를 통해 가로채 토큰 검증할 수 있음.
    // 여기에 넣기
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // StompHandler를 인터셉터로 등록하여 모든 웹소켓 메시지를 가로채고 처리
        registration.interceptors(stompHandler);
    }



}