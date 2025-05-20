package com.example.demo.domain.chat.controller;

import com.example.demo.domain.chat.model.ChatRoom;
import com.example.demo.domain.chat.service.ChatService;
import com.example.demo.domain.user.model.Users;
import com.example.demo.mapper.ChatRoomMapper;
import com.example.demo.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
@RequestMapping("/chat")
@Slf4j
public class ChatViewController {

    private final ChatService chatService;
    private final ChatRoomMapper chatRoomMapper;
    private final UserMapper userMapper;

    public ChatViewController(ChatService chatService, 
                              ChatRoomMapper chatRoomMapper,
                              UserMapper userMapper) {
        this.chatService = chatService;
        this.chatRoomMapper = chatRoomMapper;
        this.userMapper = userMapper;
    }

    //채팅 메인화면 - 목록표시
    @GetMapping("/room")
    public String chatRoomList(Model model) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            model.addAttribute("username", email);
            return "chat/room";
        } catch (Exception e) {
            log.error("채팅방 목록 페이지 로드 중 오류 발생", e);
            return "redirect:/";
        }
    }


    //채팅방 접속
    @GetMapping("/room/{roomId}")
    public String chatRoom(@PathVariable Long roomId, Model model, Principal principal) {
        log.info("채팅방 접속: roomId={}, user={}", roomId, principal.getName());
        
        try {
            // 사용자 정보 확인
            String email = principal.getName();
            Users currentUser = userMapper.findByEmail(email);
            
            if (currentUser == null) {
                log.error("사용자를 찾을 수 없음: {}", email);
                return "redirect:/login";
            }
            
            // 채팅방 정보 조회
            ChatRoom room = chatRoomMapper.findById(roomId);
            if (room == null) {
                log.error("채팅방을 찾을 수 없음: {}", roomId);
                return "redirect:/chat/my/rooms";
            }
            
            // 사용자가 채팅방 참여자인지 확인
            boolean isParticipant = chatService.isRoomParticipant(email, roomId);
            if (!isParticipant) {
                log.error("사용자가 채팅방 참여자가 아님: user={}, roomId={}", email, roomId);
                return "redirect:/chat/my/rooms";
            }
            
            String roomName = room.getRoomName();
            
            // 필요한 데이터
            model.addAttribute("roomId", roomId);
            model.addAttribute("username", email);
            model.addAttribute("roomName", roomName);
            model.addAttribute("currentUserId", currentUser.getUserId());
            
            // 읽음 상태 업데이트
            chatService.messageRead(roomId);
            
            return "chat/room";
        } catch (Exception e) {
            log.error("채팅방 페이지 로드 중 오류 발생", e);
            return "redirect:/";
        }
    }


    //1:1 채팅 개설
    @GetMapping("/oneonone")
    public String oneOnOneChat(@RequestParam Integer otherUserId, Model model, Principal principal) {
        log.info("1:1 채팅 접속 요청: otherUserId={}, 현재 사용자={}", otherUserId, principal.getName());
        
        try {
            // 현재 사용자 정보
            String email = principal.getName();
            Users currentUser = userMapper.findByEmail(email);
            
            if (currentUser == null) {
                log.error("현재 사용자를 찾을 수 없음: {}", email);
                return "redirect:/login";
            }
            
            // 상대방 정보
            Users otherUser = userMapper.findById(otherUserId);
            if (otherUser == null) {
                log.error("상대방 사용자를 찾을 수 없음: {}", otherUserId);
                return "redirect:/";
            }
            
            // 채팅방 생성 또는 조회
            Long roomId = chatService.getOrCreatePrivateRoom(otherUserId);
            log.info("채팅방 ID: {}", roomId);
            
            // 채팅방 이름 설정
            String roomName = otherUser.getNickname() + "님과의 대화";
            
            model.addAttribute("roomId", roomId);
            model.addAttribute("roomName", roomName);
            model.addAttribute("username", email);
            model.addAttribute("currentUserId", currentUser.getUserId());
            
            // 읽음 상태 업데이트
            chatService.messageRead(roomId);
            
            return "chat/room";
        } catch (Exception e) {
            log.error("1:1 채팅 페이지 로드 중 오류 발생", e);
            return "redirect:/";
        }
    }
}