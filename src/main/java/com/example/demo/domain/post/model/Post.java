package com.example.demo.domain.post.model;

import lombok.Data;

import java.util.Date;

@Data
public class Post {
    private int postId;
    private int postCategoryId;
    private int userId;
    private String title;
    private String imageId;
    private int viewCount;
    private int commentCount;
    private Date createdAt;
    private Date updatedAt;


}
