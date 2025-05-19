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
public class ReadStatus {
    private Long readId;
    private Long chatRoomId;
    private Long messageId;
    private Integer userId;
    private Boolean isRead;
    private Date createdAt;
}
