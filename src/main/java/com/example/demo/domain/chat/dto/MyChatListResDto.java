package com.example.demo.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MyChatListResDto {
    private Long roomId;
    private String roomName;
    private String isGroupChat; //이거 없애고
    //이거 추가
    //private int postId;
    private Long unReadCount;
}
