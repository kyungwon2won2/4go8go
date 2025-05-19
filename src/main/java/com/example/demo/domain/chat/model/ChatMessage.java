package com.example.demo.domain.chat.model;

import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ChatMessage {
    private Long messageId;
    private Long chatRoomId;
    private Integer userId;
    private String content;
    private Date sentAt;
}