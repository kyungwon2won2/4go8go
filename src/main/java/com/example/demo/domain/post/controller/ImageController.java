package com.example.demo.domain.post.controller;

import com.example.demo.domain.post.model.Image;
import com.example.demo.domain.post.service.ImageService;
import com.example.demo.mapper.ImageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/images")
public class ImageController {

    private final ImageService imageService;

    // 게시물 ID로 이미지 목록 조회
    @GetMapping("/post/{postId}")
    public List<Image> getImagesByPostId(@PathVariable int postId){
        return imageService.getImagesByPostId(postId);
    }

    // imageId로 단일 이미지 조회(detail.html에서 사용 중)
    @GetMapping("/{imageId}")
    public Image getImageById(@PathVariable Long imageId){
        return imageService.getImageById(imageId);
    }

}
