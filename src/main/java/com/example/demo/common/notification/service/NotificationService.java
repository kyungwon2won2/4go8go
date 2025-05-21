package com.example.demo.common.notification.service;

import com.example.demo.common.notification.controller.NotificationSseController;
import com.example.demo.common.notification.dto.NotificationDto;
import com.example.demo.common.notification.model.Notification;
import com.example.demo.domain.user.model.Users;
import com.example.demo.mapper.NotificationMapper;
import com.example.demo.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;
    
    /**
     * 알림 생성 및 전송
     */
    @Transactional
    public void createNotification(Notification notification) {
        try {
            // DB에 알림 저장
            notificationMapper.insert(notification);
            log.info("알림 저장 완료: ID={}, 수신자={}", notification.getNotificationId(), notification.getUserId());
            
            // 사용자 정보 조회
            Users user = userMapper.findById(notification.getUserId());
            if (user != null) {
                // SSE로 알림 전송 (사용자가 연결되어 있는 경우)
                if (NotificationSseController.isConnected(user.getEmail())) {
                    NotificationDto dto = convertToDto(notification);
                    NotificationSseController.sendToUser(user.getEmail(), notification.getType(), dto);
                    log.info("SSE 알림 전송 완료: 사용자={}, 타입={}", user.getEmail(), notification.getType());
                }
            }
        } catch (Exception e) {
            log.error("알림 생성 중 오류 발생", e);
        }
    }
    
    /**
     * 채팅 메시지 알림 생성
     * 알림 목록에는 추가하지 않고, 헤더의 메시지 아이콘만 업데이트하기 위해 SSE 이벤트 전송
     */
    @Transactional
    public void createChatMessageNotification(Integer receiverId, Long roomId, String senderNickname, String message) {
        try {
            // 사용자 정보 조회
            Users user = userMapper.findById(receiverId);
            if (user != null) {
                log.info("채팅 메시지 알림 준비: 수신자={}, 발신자={}, 방ID={}", user.getEmail(), senderNickname, roomId);
                
                // SSE로 채팅 알림 전송 (사용자가 연결되어 있는 경우)
                if (NotificationSseController.isConnected(user.getEmail())) {
                    // 간략한 알림 데이터 생성
                    NotificationDto dto = NotificationDto.builder()
                            .type("CHAT_MESSAGE_ICON_UPDATE")
                            .content(senderNickname + "님에게서 메시지가 왔습니다.")
                            .url("/chat/room/" + roomId)
                            .referenceId(roomId)
                            .isRead(false)
                            .build();
                    
                    // 채팅 아이콘 업데이트 이벤트 전송
                    NotificationSseController.sendToUser(user.getEmail(), "CHAT_MESSAGE_ICON_UPDATE", dto);
                    log.info("채팅 아이콘 업데이트 알림 전송 완료: 사용자={}", user.getEmail());
                } else {
                    log.info("사용자가 SSE에 연결되어 있지 않음: {}", user.getEmail());
                }
            } else {
                log.warn("채팅 알림 수신자 정보를 찾을 수 없음: receiverId={}", receiverId);
            }
        } catch (Exception e) {
            log.error("채팅 알림 생성 중 오류 발생: receiverId={}, roomId={}", receiverId, roomId, e);
        }
    }
    
    /**
     * 알림 읽음 처리
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        try {
            notificationMapper.updateReadStatus(notificationId, true);
            log.info("알림 읽음 처리 완료: {}", notificationId);
        } catch (Exception e) {
            log.error("알림 읽음 처리 중 오류 발생", e);
        }
    }
    
    /**
     * 모든 알림 읽음 처리
     */
    @Transactional
    public void markAllAsRead() {
        try {
            Users currentUser = getCurrentUser();
            if (currentUser != null) {
                notificationMapper.markAllReadByUserId(currentUser.getUserId());
                log.info("모든 알림 읽음 처리 완료: 사용자={}", currentUser.getEmail());
            }
        } catch (Exception e) {
            log.error("모든 알림 읽음 처리 중 오류 발생", e);
        }
    }
    
    /**
     * 채팅방 관련 알림 모두 읽음 처리
     */
    @Transactional
    public void markAllChatNotificationsAsRead(Long roomId) {
        try {
            Users currentUser = getCurrentUser();
            if (currentUser != null) {
                notificationMapper.markAllChatNotificationsAsRead(currentUser.getUserId(), roomId);
                log.info("채팅방 관련 알림 읽음 처리 완료: 사용자={}, 채팅방ID={}", 
                        currentUser.getEmail(), roomId);
            }
        } catch (Exception e) {
            log.error("채팅방 관련 알림 읽음 처리 중 오류 발생", e);
        }
    }
    
    /**
     * 사용자의 알림 목록 조회
     */
    public List<NotificationDto> getNotifications(int page, int size) {
        try {
            Users currentUser = getCurrentUser();
            if (currentUser == null) {
                return List.of();
            }
            
            List<Notification> notifications = notificationMapper.findByUserId(currentUser.getUserId(), page * size, size);
            return notifications.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("알림 목록 조회 중 오류 발생", e);
            return List.of();
        }
    }
    
    /**
     * 읽지 않은 알림 조회
     */
    public List<NotificationDto> getUnreadNotifications() {
        try {
            Users currentUser = getCurrentUser();
            if (currentUser == null) {
                return List.of();
            }
            
            List<Notification> notifications = notificationMapper.findUnreadByUserId(currentUser.getUserId());
            return notifications.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("읽지 않은 알림 조회 중 오류 발생", e);
            return List.of();
        }
    }

    public int countNotifications() {
        try {
            Users currentUser = getCurrentUser();
            if (currentUser == null) {
                return 0;
            }

            return notificationMapper.countByUserId(currentUser.getUserId());
        } catch (Exception e) {
            log.error("읽지 않은 알림 개수 조회 중 오류 발생", e);
            return 0;
        }
    }



    /**
     * 읽지 않은 알림 개수 조회
     */
    public int countUnreadNotifications() {
        try {
            Users currentUser = getCurrentUser();
            if (currentUser == null) {
                return 0;
            }
            
            return notificationMapper.countUnreadByUserId(currentUser.getUserId());
        } catch (Exception e) {
            log.error("읽지 않은 알림 개수 조회 중 오류 발생", e);
            return 0;
        }
    }
    
    /**
     * 현재 로그인한 사용자 정보 가져오기
     */
    private Users getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        
        String email = auth.getName();
        return userMapper.findByEmail(email);
    }
    
    /**
     * 알림 삭제
     */
    @Transactional
    public void deleteNotification(Long notificationId) {
        try {
            // 현재 사용자의 알림인지 확인
            Users currentUser = getCurrentUser();
            if (currentUser == null) {
                throw new IllegalStateException("인증된 사용자가 아닙니다.");
            }
            
            Notification notification = notificationMapper.findById(notificationId);
            if (notification == null) {
                throw new IllegalArgumentException("존재하지 않는 알림입니다.");
            }
            
            if (!notification.getUserId().equals(currentUser.getUserId())) {
                throw new IllegalArgumentException("해당 알림에 대한 권한이 없습니다.");
            }
            
            notificationMapper.deleteById(notificationId);
            log.info("알림 삭제 완료: {}", notificationId);
        } catch (Exception e) {
            log.error("알림 삭제 중 오류 발생", e);
            throw e;
        }
    }
    
    /**
     * Entity를 DTO로 변환
     */
    private NotificationDto convertToDto(Notification notification) {
        return NotificationDto.builder()
                .notificationId(notification.getNotificationId())
                .type(notification.getType())
                .content(notification.getContent())
                .url(notification.getUrl())
                .referenceId(notification.getReferenceId())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }


    public Page<Notification> getNotificationsPaged(int userId, int page, int size) {
        int offset = page * size;

        List<Notification> notifications = notificationMapper.findByUserId(userId, size, offset);
        int total = notificationMapper.countByUserId(userId);

        return new PageImpl<>(notifications, PageRequest.of(page, size), total);
    }


}