package com.example.demo.mapper;

import com.example.demo.domain.post.model.Image;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ImageMapper {
    //이미지 삽입
    void insertImage(Image image);

    //게시물 ID로 이미지 목록 조회
    List<Image> getImagesByPostId(int postId);

    //이미지 삭제
    void deleteImagesByPostId(int postId);

    Image getImageById(Long imageId);

    void deleteImageById(Long imageId);

    // 게시물 기준으로 이미지 한장 가져오기
    String selectFirstImageByPostId(int postId);
}
