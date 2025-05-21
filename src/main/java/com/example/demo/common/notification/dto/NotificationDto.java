package com.example.demo.common.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private Long notificationId;
    private String type;           // 알림 유형
    private String content;        // 알림 내용
    private String url;            // 클릭 시 이동할 URL
    private Long referenceId;      // 참조 ID
    private boolean isRead;        // 읽음 여부
    private Date createdAt;        // 생성 시간
}