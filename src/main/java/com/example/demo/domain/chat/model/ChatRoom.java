package com.example.demo.domain.chat.model;
//
//import com.example.chatserver.common.domain.BaseTimeEntity;
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Entity
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//@Getter
//public class ChatRoom extends BaseTimeEntity {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//    @Column(nullable = false)
//    private String name;
//    @Builder.Default
//    private String isGroupChat = "N";
//
//    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.REMOVE)
//    private List<ChatParticipant> participants = new ArrayList<>();
//
//    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.REMOVE, orphanRemoval = true)
//    private List<ChatMessage> chatMessageses = new ArrayList<>();
//}


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom {
    private Long chatRoomId;
    private Long postId;
    private Integer userId;
    private String isGroupChat;
    private Date createdAt;
    private String roomName;
    private String isOpen;
}
