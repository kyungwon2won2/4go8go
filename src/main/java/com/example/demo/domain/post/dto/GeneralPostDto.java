package com.example.demo.domain.post.dto;

import lombok.Data;

import java.util.Date;

@Data
public class GeneralPostDto {
    private int postId;
    private String title;
    private String userName;
    private int viewCount;
    private int commentCount;
    private Date createdAt;
}
