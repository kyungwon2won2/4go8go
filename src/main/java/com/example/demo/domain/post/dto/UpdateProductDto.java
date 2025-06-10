package com.example.demo.domain.post.dto;

import com.example.demo.domain.post.model.Image;
import com.example.demo.domain.post.model.Product;
import com.example.demo.domain.stringcode.ProductCategory;
import lombok.Data;

import java.util.List;

@Data
public class UpdateProductDto {
    private int postId;
    private int userId;
    private String title;                       // post
    private String content;                     // post
    private ProductCategory category;           // product
    private int price;                          // product
    private Product.ProductCondition condition; // product
    private List<Image> image;                  // image
}