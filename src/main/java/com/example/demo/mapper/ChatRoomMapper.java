package com.example.demo.mapper;

import com.example.demo.domain.chat.model.ChatRoom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatRoomMapper {
    ChatRoom findById(@Param("chatRoomId") Long chatRoomId);
    List<ChatRoom> findByIsGroupChat(@Param("isGroupChat") String isGroupChat);
    void insert(ChatRoom chatRoom);
    void deleteById(@Param("chatRoomId") Long chatRoomId);
}