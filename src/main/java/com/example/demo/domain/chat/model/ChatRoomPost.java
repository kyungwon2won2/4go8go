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
public class ChatRoomPost {
    private Long id;
    private Long chatRoomId;
    private int postId;
    private Date createdAt;
}
