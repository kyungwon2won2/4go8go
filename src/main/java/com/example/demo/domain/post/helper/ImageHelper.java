package com.example.demo.domain.post.helper;

import com.example.demo.domain.post.model.Image;
import com.example.demo.domain.post.service.ImageUploadService;
import com.example.demo.mapper.ImageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageHelper {

    private final ImageUploadService imageUploadService;
    private final ImageMapper imageMapper;

    public void productImageSave(MultipartFile[] imageFiles, int postId) {
        if (imageFiles != null) {
            for (MultipartFile file : imageFiles) {
                if (!file.isEmpty()) {
                    try {
                        String s3Url = imageUploadService.uploadBase64Image(
                                "data:" + file.getContentType() + ";base64," + java.util.Base64.getEncoder().encodeToString(file.getBytes()),
                                "post-images");
                        Image image = new Image();
                        image.setPostId(postId);
                        image.setUrl(s3Url);
                        imageMapper.insertImage(image);
                    } catch (IOException e) {
                        throw new RuntimeException("이미지 업로드 실패: " + e.getMessage(), e);
                    }
                }
            }
        }
    }
        // 게시물 ID로 이미지 목록 조회
        public List<Image> getImagesByPostId(int postId){
            return imageMapper.getImagesByPostId(postId);
        }
    
        // 이미지 ID로 단일 이미지 조회
        public Image getImageById(Long imageId){
            return imageMapper.getImageById(imageId);
        }

        // 게시물 ID로 단일 이미지 조회
        public String selectFirstImageByPostId(int postId){ return imageMapper.selectFirstImageByPostId(postId); }
        
        // 이미지 ID로 이미지 삭제
        public void deleteImageById(String imageUrl, Long imageId) {
            imageUploadService.deleteByUrl(imageUrl);
            imageMapper.deleteImageById(imageId);
        }

}
