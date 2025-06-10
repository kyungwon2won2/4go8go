package com.example.demo.domain.review.dto;

import lombok.Data;

@Data
public class CreateReviewDto {
    private int postId;
    private int point;
    private String content;

    // reviewerId는 세션에서, reviewedId는 post 정보에서 추출...
}