package com.example.demo.domain.post.model;

import lombok.Data;


@Data
public class Image {
    private Long imageId;
    private String url;
    private int postId;
}
