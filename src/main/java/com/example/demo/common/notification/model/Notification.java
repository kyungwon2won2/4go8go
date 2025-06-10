package com.example.demo.common.notification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    private Long notificationId;
    private Integer userId;        // 수신자 ID
    private String type;           // 알림 유형 (CHAT_MESSAGE, POST_COMMENT 등)
    private String content;        // 알림 내용
    private String url;            // 클릭 시 이동할 URL
    private Long referenceId;      // 참조 ID (채팅방 ID, 게시글 ID 등)
    private boolean isRead;        // 읽음 여부
    private Date createdAt;        // 생성 시간
    private Date readAt;           // 읽은 시간
}