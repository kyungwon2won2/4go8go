package com.example.demo.domain.chat.service;

import com.example.demo.domain.chat.dto.ChatMessageDto;
import com.example.demo.domain.chat.dto.MyChatListResDto;
import com.example.demo.domain.chat.model.*;
import com.example.demo.domain.post.service.ImageUploadService;
import com.example.demo.domain.user.model.Users;
import com.example.demo.mapper.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ChatService {

    private final ChatRoomMapper chatRoomMapper;
    private final ChatParticipantMapper chatParticipantMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final ReadStatusMapper readStatusMapper;
    private final UserMapper userMapper;
    private final ImageUploadService imageUploadService;

    //로그인한 유저 정보
    private Users getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userMapper.findByEmail(email);

        if (user == null) {
            log.error("인증된 사용자를 찾을 수 없음: {}", email);
            throw new EntityNotFoundException("user not found");
        }

        return user;
    }

    //메시지 저장, 읽음상대 변경
    public Long saveMessage(Long roomId, ChatMessageDto chatMessageDto) {
        log.info("메시지 저장 시작: roomId={}, message={}", roomId, chatMessageDto);

        try {
            // 1. 채팅방 조회
            ChatRoom chatRoom = chatRoomMapper.findById(roomId);
            if (chatRoom == null) {
                log.error("채팅방을 찾을 수 없음: {}", roomId);
                throw new EntityNotFoundException("room cannot be found");
            }

            // 2. 발신자 이메일로 유저 조회
            Users sender = userMapper.findByEmail(chatMessageDto.getSenderEmail());
            if (sender == null) {
                log.error("사용자를 찾을 수 없음: {}", chatMessageDto.getSenderEmail());
                throw new EntityNotFoundException("user not found");
            }

            if (chatMessageDto.getMessageType() == null) {
                chatMessageDto.setMessageType("TEXT");
            }

            // 3. 메시지 엔티티 생성 및 저장
            ChatMessage newMessage = ChatMessage.builder()
                    .chatRoomId(roomId)
                    .userId(sender.getUserId())
                    .content(chatMessageDto.getMessage())
                    .imageUrl(chatMessageDto.getImageUrl())
                    .messageType(chatMessageDto.getMessageType())
                    .build();

            chatMessageMapper.insert(newMessage);
            log.info("메시지 저장 완료: messageId={}", newMessage.getMessageId());

            // 저장된 메시지의 발신 시간을 DTO에 설정
            ChatMessage savedMessage = chatMessageMapper.findById(newMessage.getMessageId());
            chatMessageDto.setSentAt(savedMessage.getSentAt());

            // 4. 채팅방 참가자 별 읽음 상태 생성
            List<ChatParticipant> participants = chatParticipantMapper.findByChatRoomId(roomId);
            log.info("채팅방 참가자 수: {}", participants.size());

            for (ChatParticipant p : participants) {
                ReadStatus readStatus = ReadStatus.builder()
                        .chatRoomId(roomId)
                        .messageId(newMessage.getMessageId())
                        .userId(p.getUserId())
                        .isRead(p.getUserId().equals(sender.getUserId()))
                        .build();
                readStatusMapper.insert(readStatus);
            }

            // 5. 메시지 DTO에 발신자 닉네임 설정
            chatMessageDto.setSenderNickname(sender.getNickname());

            return newMessage.getMessageId();
        } catch (Exception e) {
            log.error("메시지 저장 중 오류 발생", e);
            throw e;
        }

    }

    //채팅방에 참여자 추가
    public void addParticipantToRoom(Long roomId, Integer userId) {
        log.info("채팅방 참가자 추가: roomId={}, userId={}", roomId, userId);

        try {
            // 이미 참가자인지 확인
            Optional<ChatParticipant> existingParticipant =
                    chatParticipantMapper.findByChatRoomIdAndUserId(roomId, userId);

            if (existingParticipant.isPresent()) {
                log.info("이미 채팅방 참가자입니다: roomId={}, userId={}", roomId, userId);
                return;
            }

            ChatParticipant participant = ChatParticipant.builder()
                    .chatRoomId(roomId)
                    .userId(userId)
                    .hasEntered(false)  // 초기값은 false로 설정
                    .build();
            chatParticipantMapper.insert(participant);
            log.info("채팅방 참가자 추가 완료");
        } catch (Exception e) {
            log.error("채팅방 참가자 추가 중 오류 발생", e);
            throw e;
        }
    }

    //채팅 내역 불러오기(메시지)
    public List<ChatMessageDto> getChatHistory(Long roomId) {
        log.info("채팅 이력 조회: roomId={}", roomId);

        try {
            Users user = getCurrentUser();

            // 참가자인지 확인
            List<ChatParticipant> participants = chatParticipantMapper.findByChatRoomId(roomId);
            boolean isParticipant = participants.stream().anyMatch(p -> p.getUserId().equals(user.getUserId()));

            if (!isParticipant) {
                log.error("채팅방 참가자가 아님: userId={}, roomId={}", user.getUserId(), roomId);
                throw new IllegalArgumentException("Not a participant");
            }

            // 메시지 조회
            List<ChatMessage> messages = chatMessageMapper.findByChatRoomId(roomId);
            log.info("조회된 메시지 수: {}", messages.size());

            List<ChatMessageDto> result = new ArrayList<>();
            for (ChatMessage msg : messages) {
                Users sender = userMapper.findById(msg.getUserId());
                result.add(ChatMessageDto.builder()
                        .message(msg.getContent())
                        .senderEmail(sender.getEmail())
                        .senderNickname(sender.getNickname())
                        .roomId(roomId)
                        .sentAt(msg.getSentAt())
                        .imageUrl(msg.getImageUrl()) // 이미지 URL 추가
                        .messageType(msg.getMessageType() != null ? msg.getMessageType() : "TEXT") // 메시지 타입 추가
                        .build());
            }

            return result;
        } catch (Exception e) {
            log.error("채팅 이력 조회 중 오류 발생", e);
            throw e;
        }
    }

    //채팅방 참가자인지 확인
    public boolean isRoomParticipant(String email, Long roomId) {
        log.info("채팅방 참가자 확인: email={}, roomId={}", email, roomId);

        try {
            Users user = userMapper.findByEmail(email);
            if (user == null) {
                log.error("사용자를 찾을 수 없음: {}", email);
                throw new EntityNotFoundException("user not found");
            }

            List<ChatParticipant> participants = chatParticipantMapper.findByChatRoomId(roomId);
            boolean isParticipant = participants.stream().anyMatch(p -> p.getUserId().equals(user.getUserId()));
            log.info("참가자 여부: {}", isParticipant);

            return isParticipant;
        } catch (Exception e) {
            log.error("채팅방 참가자 확인 중 오류 발생", e);
            throw e;
        }
    }

    //메시지 읽음처리(로그인 유저)
    public void messageRead(Long roomId) {
        log.info("메시지 읽음 처리: roomId={}", roomId);

        try {
            Users user = getCurrentUser();
            readStatusMapper.updateIsRead(roomId, user.getUserId(), true);
            log.info("메시지 읽음 처리 완료");
        } catch (Exception e) {
            log.error("메시지 읽음 처리 중 오류 발생", e);
            throw e;
        }
    }

    //채팅방 목록조회(로그인 유저)
    public List<MyChatListResDto> getMyChatRooms() {
        try {
            Users user = getCurrentUser();
            return getMyChatRooms(user.getEmail());
        } catch (Exception e) {
            log.error("내 채팅방 목록 조회 중 오류 발생", e);
            throw e;
        }
    }

    //이메일로 채팅방 목록조회
    public List<MyChatListResDto> getMyChatRooms(String email) {
        try {
            Users user = userMapper.findByEmail(email);
            if (user == null) {
                log.error("사용자를 찾을 수 없음: {}", email);
                throw new EntityNotFoundException("user not found");
            }

            log.info("이메일로 채팅방 목록 조회: email={}, userId={}", email, user.getUserId());

            List<ChatParticipant> participantList = chatParticipantMapper.findAllByUserId(user.getUserId());
            List<MyChatListResDto> result = new ArrayList<>();

            for (ChatParticipant p : participantList) {
                ChatRoom room = chatRoomMapper.findById(p.getChatRoomId());
                Long unread = readStatusMapper.countUnread(p.getChatRoomId(), user.getUserId());

                result.add(MyChatListResDto.builder()
                        .roomId(room.getChatRoomId())
                        .roomName(room.getRoomName())
                        .isGroupChat(room.getIsGroupChat())
                        .unReadCount(unread)
                        .build());
            }

            return result;
        } catch (Exception e) {
            log.error("이메일로 채팅방 목록 조회 중 오류 발생: {}", email, e);
            throw e;
        }
    }

    //채팅방 나가기
    public void leaveRoom(Long roomId) {
        try {
            Users user = getCurrentUser();
            log.info("채팅방 나가기: userId={}, roomId={}", user.getUserId(), roomId);

            ChatRoom room = chatRoomMapper.findById(roomId);
            if (room == null) {
                log.error("채팅방을 찾을 수 없음: {}", roomId);
                throw new EntityNotFoundException("room not found");
            }

            // 채팅방에서 사용자 제거
            chatParticipantMapper.deleteByRoomIdAndUserId(roomId, user.getUserId());
            log.info("채팅방에서 사용자 제거 완료");

            // 채팅방에 참가자가 모두 나갔는지 확인
            List<ChatParticipant> remain = chatParticipantMapper.findByChatRoomId(roomId);
            if (remain.isEmpty()) {
                log.info("채팅방에 참가자가 없으므로 채팅방 삭제: {}", roomId);
                chatRoomMapper.deleteById(roomId);
            } else {
                // 나가기 메시지를 채팅방에 남김
                ChatMessage leaveMessage = ChatMessage.builder()
                        .chatRoomId(roomId)
                        .userId(user.getUserId())
                        .content(user.getNickname() + "님이 채팅방을 나갔습니다.")
                        .build();
                chatMessageMapper.insert(leaveMessage);

                // 참가자들의 읽음 상태 추가
                for (ChatParticipant p : remain) {
                    ReadStatus readStatus = ReadStatus.builder()
                            .chatRoomId(roomId)
                            .messageId(leaveMessage.getMessageId())
                            .userId(p.getUserId())
                            .isRead(false)
                            .build();
                    readStatusMapper.insert(readStatus);
                }
            }
        } catch (Exception e) {
            log.error("채팅방 나가기 중 오류 발생", e);
            throw e;
        }
    }

    //1:1 채팅방 조회&생성
    public Long getOrCreatePrivateRoom(int otherUserId) {
        try {
            Users me = getCurrentUser();
            Users other = userMapper.findById(otherUserId);

            log.info("개인 채팅방 생성 또는 조회: 현재 사용자={}, 상대방={}", me.getUserId(), otherUserId);

            if (other == null) {
                log.error("상대방 사용자를 찾을 수 없음: {}", otherUserId);
                throw new EntityNotFoundException("user not found");
            }

            // 기존 채팅방 검색 - 양방향으로 검색
            Optional<Long> existingRoomId = chatParticipantMapper.findPrivateRoomBetweenUsers(me.getUserId(), other.getUserId());
            if (existingRoomId.isPresent()) {
                log.info("기존 채팅방 발견: {}", existingRoomId.get());
                return existingRoomId.get();
            }

            // 다시 한번 반대 방향으로 검색 (혹시 DB 구현 문제로 순서가 중요한 경우)
            existingRoomId = chatParticipantMapper.findPrivateRoomBetweenUsers(other.getUserId(), me.getUserId());
            if (existingRoomId.isPresent()) {
                log.info("기존 채팅방 발견(역방향): {}", existingRoomId.get());
                return existingRoomId.get();
            }

            // 새 채팅방 생성
            String roomName = me.getNickname() + "님과 " + other.getNickname() + "님의 대화";

            ChatRoom room = ChatRoom.builder()
                    .isGroupChat("N")
                    .roomName(roomName)
                    .userId(me.getUserId())
                    .build();

            chatRoomMapper.insert(room);
            log.info("새 채팅방 생성: roomId={}", room.getChatRoomId());

            // 두 참가자 추가
            addParticipantToRoom(room.getChatRoomId(), me.getUserId());
            addParticipantToRoom(room.getChatRoomId(), other.getUserId());

            return room.getChatRoomId();
        } catch (Exception e) {
            log.error("개인 채팅방 생성 중 오류 발생", e);
            throw e;
        }
    }

    //이미지 메시지 저장
    public Long saveImageMessage(Long roomId, ChatMessageDto chatMessageDto, String imageUrl) {

        chatMessageDto.setImageUrl(imageUrl);
        chatMessageDto.setMessageType(("IMAGE"));

        return saveMessage(roomId, chatMessageDto);
    }

    //이미지 조회
    public List<ChatImage> getMessageImages(Long messageId) {
            return imageUploadService.getChatImagesByMessage(messageId);
    }
}