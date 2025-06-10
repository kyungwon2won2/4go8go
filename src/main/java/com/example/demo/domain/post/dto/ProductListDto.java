package com.example.demo.domain.post.dto;

import lombok.Data;

import java.util.Date;

@Data
public class ProductListDto {
    private int postId;
    private String title;   //from post.title
    private Date createdAt; //from post.createAt
    private String location; // from user.address
    private int price;
    private String imageUrl;
}