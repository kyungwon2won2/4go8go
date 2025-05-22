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
    private String imageUrl; //이미지 경로
    private String messageType; // "TEXT" or "IMAGE"
    private Date sentAt;
}