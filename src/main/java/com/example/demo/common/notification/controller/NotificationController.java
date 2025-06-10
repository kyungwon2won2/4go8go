package com.example.demo.common.notification.controller;

import com.example.demo.common.notification.dto.NotificationDto;
import com.example.demo.common.notification.model.Notification;
import com.example.demo.common.notification.service.NotificationService;
import com.example.demo.domain.user.model.CustomerUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;
    
    /**
     * 알림 페이지
     */
    @GetMapping("/notifications")
    public String notificationsPage(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "5") int size,
                                    Model model,
                                    @AuthenticationPrincipal CustomerUser user) {
        log.info("알림 페이지 요청: 사용자={}, page={}, size={}", user.getUserId(), page, size);
        
        Page<Notification> notificationsPage = notificationService.getNotificationsPaged(user.getUserId(), page, size);
        int unreadCount = notificationService.countUnreadNotifications();

        log.info("알림 데이터 조회 결과: 총 {}개, 읽지 않은 알림 {}개", 
                notificationsPage.getTotalElements(), unreadCount);
        
        if (!notificationsPage.isEmpty()) {
            log.info("알림 데이터 샘플: 첫 번째 알림 ID={}, 내용={}, 읽음상태={}", 
                    notificationsPage.getContent().get(0).getNotificationId(),
                    notificationsPage.getContent().get(0).getContent(),
                    notificationsPage.getContent().get(0).isRead());
        } else {
            log.info("알림 데이터가 없습니다.");
        }

        model.addAttribute("notificationsPage", notificationsPage);
        model.addAttribute("notifications", notificationsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", notificationsPage.getTotalPages());
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("size", size);  // 페이지 크기 추가

        return "notification/list";
    }


    
    /**
     * 알림 목록 API
     */
    @GetMapping("/api/notifications")
    @ResponseBody
    public List<NotificationDto> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String limit) {
        
        // limit 파라미터가 제공된 경우, size로 변환
        if (limit != null && !limit.isEmpty()) {
            try {
                size = Integer.parseInt(limit);
                log.info("limit 파라미터를 size로 변환: limit={}, size={}", limit, size);
            } catch (NumberFormatException e) {
                log.warn("limit 파라미터 변환 실패, 기본 size 사용: {}", size);
            }
        }
        
        log.info("알림 목록 조회 요청: page={}, size={}", page, size);
        List<NotificationDto> notifications = notificationService.getNotifications(page, size);
        log.info("알림 목록 조회 결과: 개수={}", notifications.size());
        return notifications;
    }
    
    /**
     * 읽지 않은 알림 목록 API
     */
    @GetMapping("/api/notifications/unread")
    @ResponseBody
    public List<NotificationDto> getUnreadNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        
        log.info("읽지 않은 알림 목록 조회 요청: page={}, size={}", page, size);
        List<NotificationDto> notifications = notificationService.getUnreadNotificationsPaged(page, size);
        log.info("읽지 않은 알림 목록 조회 결과: 개수={}", notifications.size());
        return notifications;
    }
    
    /**
     * 읽지 않은 알림 개수
     */
    @GetMapping("/api/notifications/count")
    @ResponseBody
    public Map<String, Integer> getUnreadCount() {
        int count = notificationService.countUnreadNotifications();
        Map<String, Integer> response = new HashMap<>();
        response.put("count", count);
        return response;
    }
    
    /**
     * 알림 읽음 처리 (POST 방식)
     */
    @PostMapping("/api/notifications/{notificationId}/read")
    @ResponseBody
    public ResponseEntity<Map<String, String>> markAsReadPost(@PathVariable Long notificationId) {
        log.info("알림 읽음 처리(POST): {}", notificationId);
        try {
            notificationService.markAsRead(notificationId);
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("알림 읽음 처리 실패", e);
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    /**
     * 모든 알림 읽음 처리 (POST 방식)
     */
    @PostMapping("/api/notifications/read-all")
    @ResponseBody
    public ResponseEntity<Map<String, String>> markAllAsReadPost() {
        log.info("모든 알림 읽음 처리 (POST)");
        try {
            notificationService.markAllAsRead();
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("모든 알림 읽음 처리 실패", e);
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 채팅방 관련 알림 모두 읽음 처리
     */
    @PostMapping("/api/notifications/chat/{roomId}/read-all")
    @ResponseBody
    public ResponseEntity<Map<String, String>> markAllChatNotificationsAsRead(@PathVariable Long roomId) {
        if (roomId == null) {
            log.warn("채팅방 관련 알림 읽음 처리 시 roomId가 null입니다.");
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "채팅방 ID가 필요합니다.");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            log.info("채팅방 관련 알림 모두 읽음 처리: 채팅방 ID={}", roomId);
            notificationService.markAllChatNotificationsAsRead(roomId);
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("채팅방 관련 알림 읽음 처리 중 오류: roomId={}", roomId, e);
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 알림 삭제 (POST 방식)
     */
    @PostMapping("/api/notifications/{notificationId}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> deleteNotificationPost(@PathVariable Long notificationId) {
        log.info("알림 삭제(POST): {}", notificationId);
        try {
            notificationService.deleteNotification(notificationId);
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("알림 삭제 실패", e);
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
