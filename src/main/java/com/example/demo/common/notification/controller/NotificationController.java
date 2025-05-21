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
                                    @RequestParam(defaultValue = "10") int size,
                                    Model model,
                                    @AuthenticationPrincipal CustomerUser user) {
        Page<Notification> notificationsPage = notificationService.getNotificationsPaged(user.getUserId(), page, size);
        int unreadCount = notificationService.countUnreadNotifications();

        model.addAttribute("notificationsPage", notificationsPage);
        model.addAttribute("notifications", notificationsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", notificationsPage.getTotalPages());
        model.addAttribute("unreadCount", unreadCount);

        return "notification/list";
    }


    
    /**
     * 알림 목록 API
     */
    @GetMapping("/api/notifications")
    @ResponseBody
    public List<NotificationDto> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return notificationService.getNotifications(page, size);
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
