package com.example.demo.domain.review.dto;

import lombok.Data;

@Data
public class UserReviewSummaryDto {
    private int userId;
    private String nickname;
    private double averageRating;    // 평균 평점
    private int totalReviewCount;    // 총 리뷰 개수
}