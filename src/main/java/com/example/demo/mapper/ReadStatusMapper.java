package com.example.demo.mapper;

import com.example.demo.domain.chat.model.ReadStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReadStatusMapper {
    void insert(ReadStatus readStatus);
    List<ReadStatus> findByChatRoomIdAndUserId(@Param("chatRoomId") Long chatRoomId, @Param("userId") Integer userId);
    void updateIsRead(@Param("chatRoomId") Long chatRoomId, @Param("userId") Integer userId, @Param("isRead") Boolean isRead);
    Long countUnread(@Param("chatRoomId") Long chatRoomId, @Param("userId") Integer userId);
}
