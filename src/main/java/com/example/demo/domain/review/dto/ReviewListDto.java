package com.example.demo.domain.review.dto;

import lombok.Data;

import java.util.Date;

@Data
public class ReviewListDto {
    private int postId;
    private String title;              // 상품 제목
    private String reviewerNickname;
    private int point;
    private String content;
    private Date createdAt;
}