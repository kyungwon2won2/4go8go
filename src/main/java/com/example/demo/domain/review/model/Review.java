package com.example.demo.domain.review.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    private int postId;        // 거래한 상품 -pk
    private int reviewerId;    // 리뷰 작성자 (구매자) -pk
    private int reviewedId;    // 리뷰 받는 사람 (판매자)
    private int point;         // 평점 : 1-5점
    private String content;    // 리뷰 내용
    private Date createdAt;    // 작성일시
}
