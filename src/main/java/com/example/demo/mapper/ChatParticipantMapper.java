package com.example.demo.mapper;

import com.example.demo.domain.chat.model.ChatParticipant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ChatParticipantMapper {
    List<ChatParticipant> findByChatRoomId(@Param("chatRoomId") Long chatRoomId);
    Optional<ChatParticipant> findByChatRoomIdAndUserId(@Param("chatRoomId") Long chatRoomId, @Param("userId") Integer userId);
    List<ChatParticipant> findAllByUserId(@Param("userId") Integer userId);
    void insert(ChatParticipant chatParticipant);
    void updateHasEntered(@Param("chatRoomId") Long chatRoomId, @Param("userId") Integer userId, @Param("hasEntered") Boolean hasEntered);
    void delete(@Param("participantId") Long participantId);
    void deleteByRoomIdAndUserId(@Param("chatRoomId") Long chatRoomId, @Param("userId") Integer userId);

    Optional<Long> findPrivateRoomBetweenUsers(@Param("userId1") Integer userId1, @Param("userId2") Integer userId2);
}
