package com.example.demo.domain.chat.controller;

import com.example.demo.domain.chat.dto.ChatMessageDto;
import com.example.demo.domain.chat.dto.ChatRoomListResDto;
import com.example.demo.domain.chat.dto.MyChatListResDto;
import com.example.demo.domain.chat.service.ChatService;
import com.example.demo.domain.user.model.CustomerUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

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
    public ResponseEntity<?> getChatHistory(@PathVariable Long roomId){
        List<ChatMessageDto> chatMessageDtos = chatService.getChatHistory(roomId);
        return new ResponseEntity<>(chatMessageDtos, HttpStatus.OK);
    }

    //    채팅메시지 읽음처리
    @PostMapping("/room/{roomId}/read")
    public ResponseEntity<?> messageRead(@PathVariable Long roomId){
        chatService.messageRead(roomId);
        return ResponseEntity.ok().build();
    }

    //    내채팅방목록조회 : roomId, roomName, 그룹채팅여부, 메시지읽음개수
    @GetMapping("/my/rooms")
    public ResponseEntity<?> getMyChatRooms(){
        List<MyChatListResDto> myChatListResDtos = chatService.getMyChatRooms();
        return new ResponseEntity<>(myChatListResDtos, HttpStatus.OK);
    }

    //    채팅방 나가기
    @PostMapping("/room/{roomId}/leave")
    public ResponseEntity<?> leaveRoom(@PathVariable Long roomId){
        chatService.leaveRoom(roomId);
        return ResponseEntity.ok().build();
    }

    //    개인 채팅방 개설 or 기존 채팅방 roomId return
    @PostMapping("/room/private/create")
    public ResponseEntity<?> getOrCreatePrivateRoom(@RequestParam int otherMemberId, @AuthenticationPrincipal CustomerUser currentUser){
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
}