package com.example.demo.domain.chat.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitConfig {

    // 채팅방용 Topic Exchange
    @Bean
    public TopicExchange chatExchange() {
        return new TopicExchange("chat.exchange");
    }

    // 사용자 알림용 Direct Exchange  
    @Bean
    public DirectExchange userExchange() {
        return new DirectExchange("user.exchange");
    }

    // 채팅방 메시지용 Queue (동적 생성을 위한 예시)
    @Bean
    public Queue chatQueue() {
        return QueueBuilder.nonDurable("chat.room.queue").build();
    }

    // 사용자 알림용 Queue (동적 생성을 위한 예시)
    @Bean
    public Queue userQueue() {
        return QueueBuilder.nonDurable("user.notification.queue").build();
    }

    // 채팅방 Queue를 Topic Exchange에 바인딩
    @Bean
    public Binding chatBinding() {
        return BindingBuilder
                .bind(chatQueue())
                .to(chatExchange())
                .with("chat.room.*");
    }

    // 사용자 알림 Queue를 Direct Exchange에 바인딩  
    @Bean
    public Binding userBinding() {
        return BindingBuilder
                .bind(userQueue())
                .to(userExchange())
                .with("user.notification");
    }
}
