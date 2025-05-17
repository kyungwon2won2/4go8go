package com.example.demo.domain.post.model;

import lombok.Data;

@Data
public class Image {
    private int imageId;
    private int postId;
    private String imageUrl;
    private String altText;
}
