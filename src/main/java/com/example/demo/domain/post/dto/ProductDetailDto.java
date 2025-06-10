package com.example.demo.domain.post.dto;

import com.example.demo.domain.stringcode.ProductCategory;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class ProductDetailDto {
    // Post
    private int postId;
    private String title;
    private int viewCount;
    private int commentCount;
    private Date createdAt;
    private String content;

    // Product
    private ProductCategory categoryName;
    private int price;
    private String tradeStatus;
    private String condition;

    // User (seller)
    private int userId;
    private String email;
    private String nickname;
    private String address;
    private BigDecimal rating;

    // Image URLs
    private List<String> imageUrls;
}