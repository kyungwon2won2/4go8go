package com.example.demo.mapper;

import com.example.demo.common.notification.dto.NotificationDto;
import com.example.demo.common.notification.model.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationMapper {
    // 알림 ID로 개별 조회
    Notification findById(@Param("notificationId") Long notificationId);
    
    // 사용자 ID로 알림 조회 + 페이징
    List<Notification> findByUserId(@Param("userId") Integer userId, @Param("offset") int offset, @Param("limit") int limit);
    
    // 사용자의 읽지 않은 알림 조회
    List<Notification> findUnreadByUserId(@Param("userId") Integer userId);
    
    // 사용자의 읽지 않은 알림 조회 (페이징 포함)
    List<Notification> findUnreadByUserIdPaged(@Param("userId") Integer userId, @Param("offset") int offset, @Param("limit") int limit);

    //사용자의 알림 개수
    int countByUserId(@Param("userId") Integer userId);
    
    // 사용자의 읽지 않은 알림 개수
    int countUnreadByUserId(@Param("userId") Integer userId);
    
    // 알림 저장
    int insert(Notification notification);
    
    // 개별 알림 읽음 처리
    void updateReadStatus(@Param("notificationId") Long notificationId, @Param("isRead") boolean isRead);
    
    // 전체 알림 읽음 처리
    void markAllReadByUserId(@Param("userId") Integer userId);
    
    // 채팅방 관련 알림 읽음 처리
    void markAllChatNotificationsAsRead(@Param("userId") Integer userId, @Param("roomId") Long roomId);
    
    // 알림 삭제
    void deleteById(@Param("notificationId") Long notificationId);
}