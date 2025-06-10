package com.example.demo.domain.comment.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    private int commentId;
    private int postId;
    private int userId;
    private String content;
    private Date createdAt;
    private Date updatedAt;

    public Comment(int postId, int userId, String content, Date createdAt) {
        this.postId = postId;
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
    }
}
