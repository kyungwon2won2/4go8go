package com.example.demo.domain.post.dto;

import com.example.demo.domain.post.model.Product;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CreateProductDto {
    private int postId;
    private int userId;
    private String title; // 제목
    private String content; // 내용
    private Product.Category category;
    private int price;
    private Product.ProductCondition condition;
    private MultipartFile[] imageFiles; // 이미지 배열
}