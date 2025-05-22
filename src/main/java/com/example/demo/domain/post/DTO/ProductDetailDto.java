package com.example.demo.domain.post.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ProductDetailDto {
    private int postId;
    private String title;
    private String content;
    private String category;          // ex: "전자기기", "의류" 등
    private int price;
    private String condition;         // ex: "NEW", "USED" 등
    private Date createdAt;
    private String location;          // 유저 주소
    private List<String> imageUrls;   // 이미지 경로 리스트
}