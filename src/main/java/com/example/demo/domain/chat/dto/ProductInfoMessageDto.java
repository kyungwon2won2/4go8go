package com.example.demo.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductInfoMessageDto {
    private int postId;
    private String title;
    private int price;
    private String imageUrl;
    private String messageType = "PRODUCT_INFO";
}
