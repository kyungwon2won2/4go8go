package com.example.demo.domain.chat.service;

import com.example.demo.domain.chat.dto.ChatMessageDto;
import com.example.demo.domain.chat.dto.ChatParticipantDto;
import com.example.demo.domain.chat.dto.MyChatListResDto;
import com.example.demo.domain.chat.model.*;
import com.example.demo.domain.post.dto.ProductDetailDto;
import com.example.demo.domain.post.model.Post;
import com.example.demo.domain.post.service.ImageUploadService;
import com.example.demo.domain.post.service.ProductService;
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
    private final ChatRoomPostMapper chatRoomPostMapper;
    private final ProductService productService;
    private final PostMapper postMapper;

    //로그인한 유저 정보
    private Users getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userMapper.findByEmail(email);

        if (user == null) {
            throw new EntityNotFoundException("user not found");
        }

        return user;
    }

    //메시지 저장, 읽음상대 변경
    public Long saveMessage(Long roomId, ChatMessageDto chatMessageDto) {

        try {
            // 1. 채팅방 조회
            ChatRoom chatRoom = chatRoomMapper.findById(roomId);
            if (chatRoom == null) {
                throw new EntityNotFoundException("room cannot be found");
            }

            // 2. 발신자 이메일로 유저 조회
            Users sender = userMapper.findByEmail(chatMessageDto.getSenderEmail());
            if (sender == null) {
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

            // 저장된 메시지의 발신 시간을 DTO에 설정
            ChatMessage savedMessage = chatMessageMapper.findById(newMessage.getMessageId());
            chatMessageDto.setSentAt(savedMessage.getSentAt());

            // 4. 채팅방 참가자 별 읽음 상태 생성
            List<ChatParticipant> participants = chatParticipantMapper.findByChatRoomId(roomId);

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
            throw e;
        }

    }

    //채팅방에 참여자 추가
    public void addParticipantToRoom(Long roomId, Integer userId) {

        try {
            // 이미 참가자인지 확인
            Optional<ChatParticipant> existingParticipant =
                    chatParticipantMapper.findByChatRoomIdAndUserId(roomId, userId);

            if (existingParticipant.isPresent()) {
                return;
            }

            ChatParticipant participant = ChatParticipant.builder()
                    .chatRoomId(roomId)
                    .userId(userId)
                    .hasEntered(false)  // 초기값은 false로 설정
                    .build();
            chatParticipantMapper.insert(participant);
        } catch (Exception e) {
            throw e;
        }
    }

    //채팅 내역 불러오기(메시지)
    public List<ChatMessageDto> getChatHistory(Long roomId) {

        try {
            Users user = getCurrentUser();

            // 참가자인지 확인
            List<ChatParticipant> participants = chatParticipantMapper.findByChatRoomId(roomId);
            boolean isParticipant = participants.stream().anyMatch(p -> p.getUserId().equals(user.getUserId()));

            if (!isParticipant) {
                throw new IllegalArgumentException("Not a participant");
            }

            // 메시지 조회
            List<ChatMessage> messages = chatMessageMapper.findByChatRoomId(roomId);

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
            throw e;
        }
    }

    //채팅방 참가자인지 확인
    public boolean isRoomParticipant(String email, Long roomId) {

        try {
            Users user = userMapper.findByEmail(email);
            if (user == null) {
                throw new EntityNotFoundException("user not found");
            }

            List<ChatParticipant> participants = chatParticipantMapper.findByChatRoomId(roomId);

            return participants.stream().anyMatch(p -> p.getUserId().equals(user.getUserId()));
        } catch (Exception e) {
            log.error("채팅방 참가자 확인 중 오류 발생", e);
            throw e;
        }
    }

    //메시지 읽음처리(로그인 유저)
    public void messageRead(Long roomId) {

        try {
            Users user = getCurrentUser();
            readStatusMapper.updateIsRead(roomId, user.getUserId(), true);
        } catch (Exception e) {
            throw e;
        }
    }

    //채팅방 목록조회(로그인 유저)
    public List<MyChatListResDto> getMyChatRooms() {
        try {
            Users user = getCurrentUser();
            return getMyChatRooms(user.getEmail());
        } catch (Exception e) {
            throw e;
        }
    }

    //이메일로 채팅방 목록조회
    public List<MyChatListResDto> getMyChatRooms(String email) {
        try {
            Users user = userMapper.findByEmail(email);
            if (user == null) {
                throw new EntityNotFoundException("user not found");
            }

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
            throw e;
        }
    }

    //채팅방 나가기
    public ChatMessage leaveRoom(Long roomId) {
        try {
            Users user = getCurrentUser();

            ChatMessage leaveMessage = null;

            ChatRoom room = chatRoomMapper.findById(roomId);
            if (room == null) {
                throw new EntityNotFoundException("room not found");
            }

            // 채팅방에서 사용자 제거
            chatParticipantMapper.deleteByRoomIdAndUserId(roomId, user.getUserId());

            // 채팅방에 참가자가 모두 나갔는지 확인
            List<ChatParticipant> remain = chatParticipantMapper.findByChatRoomId(roomId);
            if (remain.isEmpty()) {
                chatRoomMapper.deleteById(roomId);
            } else {
                // 나가기 메시지를 채팅방에 남김
                leaveMessage = ChatMessage.builder()
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
            return leaveMessage;
        } catch (Exception e) {
            throw e;
        }

    }

    //1:1 채팅방 조회&생성
    public Long getOrCreatePrivateRoom(int otherUserId) {
        return getOrCreatePrivateRoom(otherUserId, 0);
    }

    //1:1 채팅방 조회&생성 (상품 ID 포함)
    public Long getOrCreatePrivateRoom(int otherUserId, int postId) {
        try {
            Users me = getCurrentUser();
            Users other = userMapper.findById(otherUserId);

            if (other == null) {
                throw new EntityNotFoundException("user not found");
            }

            Long roomId = null;
            boolean isNewRoom = false;
            boolean isNewRelation = false;

            // 기존 채팅방 검색 - 양방향으로 검색
            Optional<Long> existingRoomId = chatParticipantMapper.findPrivateRoomBetweenUsers(me.getUserId(), other.getUserId());
            if (existingRoomId.isPresent()) {
                roomId = existingRoomId.get();
            } else {
                // 다시 한번 반대 방향으로 검색 (혹시 DB 구현 문제로 순서가 중요한 경우)
                existingRoomId = chatParticipantMapper.findPrivateRoomBetweenUsers(other.getUserId(), me.getUserId());
                if (existingRoomId.isPresent()) {
                    roomId = existingRoomId.get();
                } else {
                    // 새 채팅방 생성
                    String roomName = me.getNickname() + "님과 " + other.getNickname() + "님의 대화";

                    ChatRoom room = ChatRoom.builder()
                            .isGroupChat("N")
                            .roomName(roomName)
                            .userId(me.getUserId())
                            .postId(postId) // 기존 postId 필드는 유지 (DB 호환성을 위해)
                            .build();

                    chatRoomMapper.insert(room);
                    roomId = room.getChatRoomId();

                    // 두 참가자 추가
                    addParticipantToRoom(roomId, me.getUserId());
                    addParticipantToRoom(roomId, other.getUserId());
                    isNewRoom = true;
                }
            }

            // postId가 유효한 경우에만 (0이 아닌 경우) chat_room_post 테이블에 추가
            if (postId > 0) {
                // 이미 이 채팅방과 게시글의 연결이 있는지 확인
                Optional<ChatRoomPost> existingRelation = 
                        chatRoomPostMapper.findByChatRoomIdAndPostId(roomId, postId);
                
                if (!existingRelation.isPresent()) {
                    // 없으면 새로 추가
                    ChatRoomPost chatRoomPost = ChatRoomPost.builder()
                            .chatRoomId(roomId)
                            .postId(postId)
                            .build();
                    chatRoomPostMapper.insert(chatRoomPost);
                    
                    // 새로운 게시글 관계가 생성되었을 때 상품 정보를 메시지로 전송
                    sendProductInfoMessage(roomId, postId, me.getUserId());
                }
            }

            return roomId;
        } catch (Exception e) {
            throw e;
        }
    }
    
    // 상품 정보를 메시지로 전송하는 메서드
    private void sendProductInfoMessage(Long roomId, int postId, Integer senderId) {
        try {
            // 상품 정보 조회
            ProductDetailDto product = productService.getProductDetailByPostId(postId);
            if (product == null) {
                return;
            }
            
            // 상품 정보를 포함하는 메시지 생성
            String imageUrl = "";
            if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
                imageUrl = product.getImageUrls().get(0);
            }
            
            // 상품 정보 메시지 내용 구성
            String content = String.format(
                "📦 상품 정보\n" +
                "제목: %s\n" +
                "가격: %,d원",
                product.getTitle(),
                product.getPrice()
            );
            
            // 메시지 생성 및 저장
            ChatMessage message = ChatMessage.builder()
                    .chatRoomId(roomId)
                    .userId(senderId)
                    .content(content)
                    .messageType("PRODUCT_INFO")
                    .imageUrl(imageUrl)
                    .build();
            
            chatMessageMapper.insert(message);
            
            // 채팅방 참가자들의 읽음 상태 생성
            List<ChatParticipant> participants = chatParticipantMapper.findByChatRoomId(roomId);
            for (ChatParticipant p : participants) {
                ReadStatus readStatus = ReadStatus.builder()
                        .chatRoomId(roomId)
                        .messageId(message.getMessageId())
                        .userId(p.getUserId())
                        .isRead(p.getUserId().equals(senderId)) // 발신자는 읽음 처리
                        .build();
                readStatusMapper.insert(readStatus);
            }
        } catch (Exception e) {
            log.error("상품 정보 메시지 전송 중 오류 발생", e);
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

    //해당 글에 연결된 채팅방의 개수
    public int countChatRoom(int postId) {
        return chatRoomPostMapper.countChatRoomsByPostId(postId);
    }

    // ChatService.java에 추가할 메서드
    public List<ChatParticipantDto> getChatParticipantsByPostId(int postId) {
        // 1. 해당 상품의 판매자 ID 조회
        Post post = postMapper.selectPostById(postId);
        if (post == null) {
            throw new IllegalArgumentException("존재하지 않는 상품입니다.");
        }
        int sellerId = post.getUserId();

        // 2. 채팅 참여자 조회 (판매자 제외, 중복 제거)
        return chatParticipantMapper.selectParticipantsByPostId(postId, sellerId);
    }

    public boolean isValidBuyer(int postId, int buyerId) {
        // 1. 해당 상품의 채팅 참여자인지 확인
        List<ChatParticipantDto> participants = getChatParticipantsByPostId(postId);

        // 2. buyerId가 참여자 목록에 있는지 확인
        return participants.stream()
                .anyMatch(participant -> participant.getUserId() == buyerId);
    }
}