package com.example.demo.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDto {
    private Long roomId;
    private String message;
    private String senderEmail;
    private String senderNickname;
    private Date sentAt; // 메시지 발신 시간 필드 추가
}
