package com.example.demo.domain.post.service;

import com.example.demo.domain.post.model.Image;
import com.example.demo.mapper.ImageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageMapper imageMapper;

    //이미지 삽입
    public void saveImage(Image image) {
        imageMapper.insertImage(image);
    }

    // 게시물 ID로 이미지 목록 조회
    public List<Image> getImagesByPostId(int postId){
        return imageMapper.getImagesByPostId(postId);
    }

    // 이미지 ID로 단일 이미지 조회
    public Image getImageById(Long imageId){
        return imageMapper.getImageById(imageId);
    }

    // 게시물 아이디로 이미지 삭제
    public void deleteImages(int postId){
        imageMapper.deleteImages(postId);
    }

}
