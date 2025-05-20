package com.example.demo.domain.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
    private int commentId;
    private int postId;
    private int userId;
    private String nickname;
    private String commentContent;
    private Date createdAt;
    private Date updatedAt;

    @Override
    public String toString() {
        return "CommentDTO{" +
                "commentId=" + commentId +
                ", postId=" + postId +
                ", userId=" + userId +
                ", nickname='" + nickname + '\'' +
                ", commentContent='" + commentContent + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
