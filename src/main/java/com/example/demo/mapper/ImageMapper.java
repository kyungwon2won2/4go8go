package com.example.demo.mapper;

import com.example.demo.domain.post.model.Image;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ImageMapper {
    void insertImage(Image image);
    void deleteImagesByPostId(int postId);
    List<Image> selectImagesByPostId(int postId);
}
