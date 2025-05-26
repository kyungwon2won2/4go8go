package com.example.demo.domain.review.dto;

import lombok.Data;

import java.util.Date;

@Data
public class ReviewDetailDto {
    private int postId;
    private String title;
    private String reviewerNickname;
    private String reviewedNickname;
    private int point;
    private String content;
    private Date createdAt;
}
