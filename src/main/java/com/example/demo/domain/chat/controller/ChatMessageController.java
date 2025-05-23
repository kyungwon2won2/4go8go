package com.example.demo.domain.chat.controller;

import com.example.demo.common.notification.service.NotificationService;
import com.example.demo.domain.chat.dto.ChatMessageDto;
import com.example.demo.domain.chat.dto.MyChatListResDto;
import com.example.demo.domain.chat.model.ChatMessage;
import com.example.demo.domain.chat.model.ChatParticipant;
import com.example.demo.domain.chat.service.ChatService;
import com.example.demo.domain.user.model.Users;
import com.example.demo.mapper.ChatMessageMapper;
import com.example.demo.mapper.ChatParticipantMapper;
import com.example.demo.mapper.UserMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;

@Controller
@Slf4j
public class ChatMessageController {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final UserMapper userMapper;
    private final ChatParticipantMapper chatParticipantMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final com.example.demo.common.notification.service.NotificationService notificationService;
    
    public ChatMessageController(
            SimpMessagingTemplate messagingTemplate,
            ChatService chatService,
            UserMapper userMapper, 
            ChatParticipantMapper chatParticipantMapper,
            ChatMessageMapper chatMessageMapper,
            com.example.demo.common.notification.service.NotificationService notificationService) {
        this.messagingTemplate = messagingTemplate;
        this.chatService = chatService;
        this.userMapper = userMapper;
        this.chatParticipantMapper = chatParticipantMapper;
        this.chatMessageMapper = chatMessageMapper;
        this.notificationService = notificationService;
    }
    
    /**
     * 채팅방 입장 메시지 처리
     */
    @MessageMapping("/chat/enter")
    public void enter(@Payload ChatMessageDto message, SimpMessageHeaderAccessor headerAccessor) {
        try {
            // 사용자 인증 정보 확인
            String email = null;
            
            // 헤더에서 사용자 정보 확인
            if (headerAccessor.getUser() != null) {
                email = headerAccessor.getUser().getName();
            } 
            // 메시지에서 이메일 정보 확인
            else if (message.getSenderEmail() != null && !message.getSenderEmail().isEmpty()) {
                email = message.getSenderEmail();
            }
            // 컨텍스트에서 인증 정보 확인
            else {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null) {
                    email = auth.getName();
                }
            }
            
            if (email != null) {
                // 사용자 정보 조회 및 닉네임 설정
                Users user = userMapper.findByEmail(email);
                if (user != null) {
                    message.setSenderEmail(email);
                    message.setSenderNickname(user.getNickname());
                    
                    // 사용자가 이미 입장했는지 확인
                    Optional<ChatParticipant> participant =
                        chatParticipantMapper.findByChatRoomIdAndUserId(message.getRoomId(), user.getUserId());
                    
                    if (participant.isPresent()) {
                        ChatParticipant chatParticipant = participant.get();
                        
                        // 처음 입장하는 경우에만 입장 메시지 전송
                        if (chatParticipant.getHasEntered() == null || !chatParticipant.getHasEntered()) {
                            // RabbitMQ용 destination으로 입장 메시지 브로드캐스팅
                            messagingTemplate.convertAndSend("/topic/chat.room." + message.getRoomId(), message);
                            
                            // 입장 상태 업데이트
                            chatParticipantMapper.updateHasEntered(message.getRoomId(), user.getUserId(), true);
                        }
                    } else {
                        log.error("채팅방 참가자 정보를 찾을 수 없음: roomId={}, userId={}", 
                                message.getRoomId(), user.getUserId());
                    }
                }
            }
        } catch (Exception e) {
            log.error("입장 메시지 처리 오류", e);
        }
    }
    
    /**
     * 채팅 메시지 처리
     */
    @MessageMapping("/chat/message")
    public void sendMessage(@Payload ChatMessageDto message, SimpMessageHeaderAccessor headerAccessor) {
        try {
            // 사용자 인증 정보
            String email = null;
            
            // 헤더에서 사용자 정보 가져오기
            if (headerAccessor.getUser() != null) {
                email = headerAccessor.getUser().getName();
            } 
            // 메시지에서 이메일 확인
            else if (message.getSenderEmail() != null && !message.getSenderEmail().isEmpty()) {
                email = message.getSenderEmail();
            }
            // 컨텍스트에서 인증 정보 확인
            else {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null) {
                    email = auth.getName();
                }
            }
            
            if (email == null) {
                log.error("사용자 이메일을 확인할 수 없습니다.");
                return;
            }
            
            // 사용자 정보 조회 및 닉네임 설정
            Users user = userMapper.findByEmail(email);
            if (user != null) {
                message.setSenderEmail(email);
                message.setSenderNickname(user.getNickname());
            }
            
            // 메시지 저장
            if (message.getRoomId() != null) {
                Long messageId = chatService.saveMessage(message.getRoomId(), message);
                
                // 저장된 메시지의 시간 정보 가져오기
                ChatMessage savedMessage = chatMessageMapper.findById(messageId);
                message.setSentAt(savedMessage.getSentAt());
                
                // RabbitMQ용 destination으로 메시지 브로드캐스팅
                messagingTemplate.convertAndSend("/topic/chat.room." + message.getRoomId(), message);
                
                // 참가자들에게 읽지 않은 메시지 개수 업데이트 알림
                broadcastUnreadCountUpdates(message.getRoomId());
                
                // 채팅방 참가자들에게 알림 생성 (추가)
                sendChatNotifications(message, user.getUserId());
            } else {
                log.error("roomId가 null입니다.");
            }
        } catch (Exception e) {
            log.error("메시지 처리 오류", e);
        }
    }
    
    /**
     * 채팅 알림 전송
     */
    private void sendChatNotifications(ChatMessageDto message, Integer senderId) {
        try {
            // 채팅방 참가자 조회
            List<ChatParticipant> participants = chatParticipantMapper.findByChatRoomId(message.getRoomId());
            
            for (ChatParticipant participant : participants) {
                // 발신자 본인은 제외
                if (participant.getUserId().equals(senderId)) {
                    continue;
                }
                
                // 알림 생성
                notificationService.createChatMessageNotification(
                    participant.getUserId(),
                    message.getRoomId(),
                    message.getSenderNickname(),
                    message.getMessage()
                );
            }
        } catch (Exception e) {
            log.error("채팅 알림 생성 중 오류", e);
        }
    }
    
    /**
     * 참가자들에게 읽지 않은 메시지 개수 업데이트 알림
     */
    private void broadcastUnreadCountUpdates(Long roomId) {
        try {
            // 채팅방 참가자 조회
            List<ChatParticipant> participants = chatParticipantMapper.findByChatRoomId(roomId);
            
            // 각 참가자별로 읽지 않은 메시지 개수 업데이트 알림
            for (ChatParticipant participant : participants) {
                // 사용자 정보 조회
                Users user = userMapper.findById(participant.getUserId());
                if (user != null) {
                    // 사용자의 모든 채팅방에 대한 읽지 않은 메시지 개수 조회
                    List<MyChatListResDto> userRooms = chatService.getMyChatRooms(user.getEmail());
                    
                    // RabbitMQ용 개인별 알림 채널로 전송
                    messagingTemplate.convertAndSend("/queue/user." + user.getEmail() + ".unread", userRooms);
                }
            }
        } catch (Exception e) {
            log.error("읽지 않은 메시지 업데이트 전송 오류", e);
        }
    }

    /**
     * 룸 ID로 직접 메시지 전송
     */
    @MessageMapping("/{roomId}")
    public void sendMessageToRoom(@DestinationVariable Long roomId, @Payload ChatMessageDto message) throws JsonProcessingException {
        try {
            // 세션에서 인증된 사용자 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            // 사용자 정보 조회 및 닉네임 설정
            Users user = userMapper.findByEmail(email);
            if (user != null) {
                message.setSenderEmail(email);
                message.setSenderNickname(user.getNickname());
            }

            // roomId DTO에 세팅
            message.setRoomId(roomId);
            
            // 메시지 저장
            Long messageId = chatService.saveMessage(roomId, message);
            
            // 저장된 메시지의 시간 정보 가져오기
            ChatMessage savedMessage = chatMessageMapper.findById(messageId);
            message.setSentAt(savedMessage.getSentAt());
            
            // RabbitMQ용 destination으로 메시지 브로드캐스팅
            messagingTemplate.convertAndSend("/topic/chat.room." + roomId, message);
        } catch (Exception e) {
            log.error("직접 메시지 전송 오류", e);
        }
    }
}