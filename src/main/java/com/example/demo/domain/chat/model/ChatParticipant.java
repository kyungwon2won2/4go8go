package com.example.demo.domain.chat.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatParticipant {
    private Long participantId;
    private Long chatRoomId;
    private Integer userId;
    private Date createdAt;
    private Boolean hasEntered;
}
