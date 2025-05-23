package com.example.demo.domain.chat.controller;

import com.example.demo.domain.chat.dto.ChatMessageDto;
import com.example.demo.domain.chat.dto.MyChatListResDto;
import com.example.demo.domain.chat.model.ChatMessage;
import com.example.demo.domain.chat.model.ChatParticipant;
import com.example.demo.domain.chat.service.ChatService;
import com.example.demo.domain.post.service.ImageUploadService;
import com.example.demo.domain.user.model.CustomerUser;
import com.example.demo.domain.user.model.Users;
import com.example.demo.mapper.ChatMessageMapper;
import com.example.demo.mapper.ChatParticipantMapper;
import com.example.demo.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;
    private final ImageUploadService imageUploadService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageMapper chatMessageMapper;
    private final ChatParticipantMapper chatParticipantMapper;
    private final UserMapper userMapper;

//    //그룹 채팅방 개설
//    @PostMapping("/room/group/create")
//    public ResponseEntity<?> createGroup(@RequestParam String roomName) {
//        chatService.createGroupRoom(roomName);
//        return ResponseEntity.ok().build();
//    }

//    //    그룹채팅목록조회
//    @GetMapping("/room/group/list")
//    public ResponseEntity<?> getGroupChatRooms(){
//        List<ChatRoomListResDto> chatRooms = chatService.getGroupchatRooms();
//        return new ResponseEntity<>(chatRooms, HttpStatus.OK);
//    }
//
//    //    그룹채팅방참여
//    @PostMapping("/room/group/{roomId}/join")
//    public ResponseEntity<?> joinGroupChatRoom(@PathVariable Long roomId){
//        chatService.addParticipantToGroupChat(roomId);
//        return ResponseEntity.ok().build();
//    }
//
//    //    그룹 채팅방 나가기
//    @DeleteMapping("/room/group/{roomId}/leave")
//    public ResponseEntity<?> leaveGroupChatRoom(@PathVariable Long roomId){
//        chatService.leaveGroupChatRoom(roomId);
//        return ResponseEntity.ok().build();
//    }

    //    이전 메시지 조회
    @GetMapping("/history/{roomId}")
    public ResponseEntity<?> getChatHistory(@PathVariable Long roomId) {
        if (roomId == null) {
            log.warn("채팅 이력 조회 시 roomId가 null입니다.");
            return ResponseEntity.badRequest().body("유효하지 않은 채팅방 ID입니다.");
        }

        try {
            List<ChatMessageDto> chatMessageDtos = chatService.getChatHistory(roomId);
            return new ResponseEntity<>(chatMessageDtos, HttpStatus.OK);
        } catch (Exception e) {
            log.error("채팅 이력 조회 오류: roomId={}", roomId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("채팅 이력 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    //    채팅메시지 읽음처리
    @PostMapping("/room/{roomId}/read")
    public ResponseEntity<?> messageRead(@PathVariable Long roomId) {
        if (roomId == null) {
            log.warn("메시지 읽음 처리 시 roomId가 null입니다.");
            return ResponseEntity.badRequest().body("유효하지 않은 채팅방 ID입니다.");
        }

        try {
            chatService.messageRead(roomId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("메시지 읽음 처리 오류: roomId={}", roomId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("메시지 읽음 처리 중 오류가 발생했습니다.");
        }
    }

    //    내채팅방목록조회 : roomId, roomName, 그룹채팅여부, 메시지읽음개수
    @GetMapping("/my/rooms")
    public ResponseEntity<?> getMyChatRooms() {
        try {
            List<MyChatListResDto> myChatListResDtos = chatService.getMyChatRooms();
            return new ResponseEntity<>(myChatListResDtos, HttpStatus.OK);
        } catch (Exception e) {
            log.error("채팅방 목록 조회 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("채팅방 목록 조회 중 오류가 발생했습니다.");
        }
    }

    //    채팅방 나가기
    @PostMapping("/room/{roomId}/leave")
    public ResponseEntity<?> leaveRoom(@PathVariable Long roomId) {
        if (roomId == null) {
            log.warn("채팅방 나가기 시 roomId가 null입니다.");
            return ResponseEntity.badRequest().body("유효하지 않은 채팅방 ID입니다.");
        }

        try {
            chatService.leaveRoom(roomId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("채팅방 나가기 오류: roomId={}", roomId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("채팅방 나가기 중 오류가 발생했습니다.");
        }
    }

    //    개인 채팅방 개설 or 기존 채팅방 roomId return
    @PostMapping("/room/private/create")
    public ResponseEntity<?> getOrCreatePrivateRoom(@RequestParam int otherMemberId, @AuthenticationPrincipal CustomerUser currentUser) {
        try {
            if (otherMemberId == currentUser.getUserId()) {
                return ResponseEntity.badRequest().body("자신과는 채팅할 수 없습니다.");
            }

            Long roomId = chatService.getOrCreatePrivateRoom(otherMemberId);
            log.info("채팅방 생성 결과: roomId={}, 요청자={}, 상대방={}",
                    roomId, currentUser.getUserId(), otherMemberId);

            return new ResponseEntity<>(roomId, HttpStatus.OK);
        } catch (Exception e) {
            log.error("채팅방 생성 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("채팅방 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    //이미지 전송
    @PostMapping("/send-image")
    public ResponseEntity<?> sendImageMessage(@RequestParam("file") MultipartFile file,
                                              @RequestParam("roomId") Long roomId,
                                              @AuthenticationPrincipal CustomerUser currentUser) {
        try {
            // 파일 검증
            if (file.isEmpty() || !file.getContentType().startsWith("image/")) {
                return ResponseEntity.badRequest().body("유효하지 않은 이미지 파일입니다.");
            }

            // 파일 크기 검증 (10MB 제한)
            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.badRequest().body("파일 크기는 10MB를 초과할 수 없습니다.");
            }

            // 1. 먼저 S3에 이미지만 업로드 (CHAT_IMAGE 테이블에는 저장하지 않음)
            String imageUrl = imageUploadService.uploadBase64Image(
                    imageUploadService.convertToBase64(file), "chat"
            );

            // 2. 이미지 메시지 DTO 생성
            ChatMessageDto messageDto = ChatMessageDto.builder()
                    .roomId(roomId)
                    .senderEmail(currentUser.getUsername())
                    .message("") // 이미지 메시지는 텍스트 없음
                    .messageType("IMAGE")
                    .imageUrl(imageUrl)
                    .build();

            // 3. 사용자 정보 조회 및 닉네임 설정
            Users user = userMapper.findByEmail(currentUser.getUsername());
            if (user != null) {
                messageDto.setSenderNickname(user.getNickname());
            }

            // 4. 이미지 메시지 저장 (메시지 생성 + 이미지 정보 연결)
            Long messageId = chatService.saveImageMessage(roomId, messageDto, imageUrl);

            // 5. 저장된 메시지의 시간 정보 가져오기
            ChatMessage savedMessage = chatMessageMapper.findById(messageId);
            messageDto.setSentAt(savedMessage.getSentAt());

            // 6. RabbitMQ용 destination으로 이미지 메시지 브로드캐스트
            messagingTemplate.convertAndSend("/topic/chat.room." + roomId, messageDto);
            log.info("이미지 메시지 WebSocket 브로드캐스트 완료: roomId={}, messageId={}", roomId, messageId);

            // 7. 참가자들에게 읽지 않은 메시지 개수 업데이트 알림
            broadcastUnreadCountUpdates(roomId);

            return ResponseEntity.ok().body(Map.of(
                    "messageId", messageId,
                    "imageUrl", imageUrl,
                    "success", true
            ));

        } catch (Exception e) {
            log.error("이미지 메시지 전송 실패", e);
            return ResponseEntity.internalServerError().body("이미지 전송에 실패했습니다.");
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

}