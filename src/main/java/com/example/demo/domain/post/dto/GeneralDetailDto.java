package com.example.demo.domain.post.dto;

import lombok.Data;

import java.util.Date;

@Data
public class GeneralDetailDto {
    private int postId;
    private int postCategoryId;
    private String title;
    private String userName;
    private String content;
    private int viewCount;
    private int commentCount;
    private Date createdAt;
    private Date updatedAt;
    private int userId;
}
