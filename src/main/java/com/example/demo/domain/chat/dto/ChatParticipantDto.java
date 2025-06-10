package com.example.demo.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatParticipantDto {
    private int userId;
    private String nickname;
    private String email;
    private String profileImage;
}