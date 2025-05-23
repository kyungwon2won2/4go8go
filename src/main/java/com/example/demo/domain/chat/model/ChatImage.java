package com.example.demo.domain.chat.model;

import lombok.*;
import java.util.Date;


/*
    [채팅 이미지 전송 플로우]

        현재 잘못된 플로우
    1. 이미지 파일 업로드 → S3 업로드 → CHAT_IMAGE 테이블에 MESSAGE_ID=null로 저장 x
    2. 메시지 생성 → CHAT_MESSAGE 테이블에 저장
    3. WebSocket 전송
        올바른 플로우
    1. 먼저 메시지 생성 → CHAT_MESSAGE 테이블에 저장 → MESSAGE_ID 획득
    2. 이미지 업로드 → S3 업로드 → CHAT_IMAGE 테이블에 MESSAGE_ID와 함께 저장
    3. WebSocket 전송
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ChatImage {
    private Long chatImageId;
    private Long messageId;
    private String imageUrl;
    private String originalName;
    private Long fileSize;
    private String contentType;
    private Date uploadDate;
    private Boolean isDeleted;
}