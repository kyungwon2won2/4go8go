package com.example.demo.domain.post.dto;

import com.example.demo.domain.post.model.Product;
import com.example.demo.domain.stringcode.ProductCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyProductDto {
    private int postId;             // 게시글 ID
    private String title;           // 제목
    private String content;         // 내용
    private int price;              // 가격
    private ProductCategory category; // 카테고리
    private Product.TradeStatus tradeStatus; // 거래 상태
    private Product.ProductCondition condition; // 상품 상태
    private int viewCount;          // 조회수
    private int commentCount;       // 댓글수
    private int chatCount;          // 채팅방수
    private int likeCount;          // 찜수
    private Date createdAt;         // 등록일
    private String location;        // 위치 정보
    private String imageUrl;        // 대표 이미지 URL
    private boolean hasMultipleImages; // 이미지가 여러장인지 여부
}
