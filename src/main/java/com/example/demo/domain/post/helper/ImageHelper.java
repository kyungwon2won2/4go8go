package com.example.demo.domain.post.helper;

import com.example.demo.domain.post.model.Image;
import com.example.demo.domain.post.service.ImageUploadService;
import com.example.demo.mapper.ImageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageHelper {

    private final ImageUploadService imageUploadService;
    private final ImageMapper imageMapper;

    /**
     * 이미지를 1:1 비율로 변환한다.
     * 이미지가 정사각형이 아닌 경우, 중앙에서 크롭하여 정사각형으로 만든다.
     *
     * @param file 원본 이미지 파일
     * @return 1:1 비율로 처리된 이미지의 바이트 배열
     * @throws IOException 이미지 처리 중 발생할 수 있는 예외
     */
    public byte[] processToSquareImage(MultipartFile file) throws IOException {
        // MultipartFile을 BufferedImage로 변환
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
        
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        
        // 이미 정사각형인 경우 그대로 반환
        if (width == height) {
            return file.getBytes();
        }
        
        // 크기 결정 (작은 쪽 기준)
        int size = Math.min(width, height);
        
        // 크롭할 시작 위치 계산 (중앙에서 크롭)
        int x = 0;
        int y = 0;
        
        if (width > height) {
            // 가로가 더 긴 경우
            x = (width - size) / 2;
        } else {
            // 세로가 더 긴 경우
            y = (height - size) / 2;
        }
        
        // 중앙에서 크롭하여 정사각형 이미지 생성
        BufferedImage squareImage = originalImage.getSubimage(x, y, size, size);
        
        // BufferedImage를 바이트 배열로 변환
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String formatName = file.getContentType().split("/")[1]; // image/jpeg -> jpeg
        ImageIO.write(squareImage, formatName, baos);
        
        return baos.toByteArray();
    }

    public void productImageSave(MultipartFile[] imageFiles, int postId) {
        if (imageFiles != null) {
            for (MultipartFile file : imageFiles) {
                if (!file.isEmpty()) {
                    try {
                        // 이미지를 1:1 비율로 처리
                        byte[] squareImageBytes = processToSquareImage(file);
                        
                        String s3Url = imageUploadService.uploadBase64Image(
                                "data:" + file.getContentType() + ";base64," + java.util.Base64.getEncoder().encodeToString(squareImageBytes),
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
