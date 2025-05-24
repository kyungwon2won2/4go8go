package com.example.demo.mapper;

import com.example.demo.domain.chat.model.ChatRoomPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ChatRoomPostMapper {
    void insert(ChatRoomPost chatRoomPost);
    Optional<ChatRoomPost> findByChatRoomIdAndPostId(@Param("chatRoomId") Long chatRoomId, @Param("postId") int postId);
    List<ChatRoomPost> findByChatRoomId(@Param("chatRoomId") Long chatRoomId);
    List<ChatRoomPost> findByPostId(@Param("postId") int postId);
    int countChatRoomsByPostId(@Param("postId") int postId);
}
