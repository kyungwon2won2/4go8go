package com.example.demo.mapper;

import com.example.demo.domain.chat.model.ChatRoom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatRoomMapper {
    ChatRoom findById(@Param("chatRoomId") Long chatRoomId);
    void insert(ChatRoom chatRoom);
    void deleteById(@Param("chatRoomId") Long chatRoomId);
    int countChatRoomByPostId(@Param("postId") int postId);
    void updatePostId(@Param("chatRoomId") Long chatRoomId, @Param("postId") int postId);
}