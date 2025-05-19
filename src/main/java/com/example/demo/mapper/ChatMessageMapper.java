package com.example.demo.mapper;


import com.example.demo.domain.chat.model.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMessageMapper {
    ChatMessage findById(@Param("messageId") Long messageId);
    List<ChatMessage> findByChatRoomId(@Param("chatRoomId") Long chatRoomId);
    void insert(ChatMessage chatMessage);
    void deleteById(@Param("messageId") Long messageId);
}