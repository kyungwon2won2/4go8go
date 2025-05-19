package com.example.demo.domain.post.model;

import lombok.Data;

import java.util.Date;

@Data
public class Post {
    private int postId;
    private int postCategoryId;
    private int userId;
    private String title;
    //private String imageId; 스키마 컬럼삭제
    private int viewCount;
    private int commentCount;
    private Date createdAt;
    private Date updatedAt;

    //내용추가, 스키마 컬럼추가
    private String content;


}
