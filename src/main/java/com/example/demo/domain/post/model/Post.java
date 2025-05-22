package com.example.demo.domain.post.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
public class Post {
    private int postId;
    private int postCategoryId;
    private int userId;
    private String title;
    private int viewCount;
    private int commentCount;
    private Date createdAt;
    private Date updatedAt;

    //내용추가, 스키마 컬럼추가
    private String content;

    public Post(int postCategoryId, int userId, String title) {
        this.postCategoryId = postCategoryId;
        this.userId = userId;
        this.title = title;
        this.viewCount = 0;
        this.createdAt = new Date();
    }
}
