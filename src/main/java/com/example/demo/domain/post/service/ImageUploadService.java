package com.example.demo.domain.post.service;

import com.example.demo.domain.chat.model.ChatImage;
import com.example.demo.mapper.ImageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageUploadService {

    private final S3Client s3Client;
    private final ImageMapper imageMapper;

    @Value("${aws.s3.bucket}")
    private String bucket;

    public String uploadBase64Image(String base64Data, String folder) throws IOException {
        String[] parts = base64Data.split(",");
        String metadata = parts[0];
        String data = parts[1];

        String mimeType = metadata.split(";")[0].split(":")[1];
        String extension = mimeType.split("/")[1];

        byte[] bytes = Base64.getDecoder().decode(data);
        String filename = folder + "/" + UUID.randomUUID() + "." + extension;

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(filename)
                .contentType(mimeType)
                .acl("public-read")
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(bytes));

        return s3Client.utilities().getUrl(GetUrlRequest.builder()
                .bucket(bucket)
                .key(filename)
                .build()).toExternalForm();
    }

    public void deleteByUrl(String imageUrl) {
        try{
            // 1. 실제 key만 잘라내기
            URI uri = URI.create(imageUrl);
            String key = uri.getPath().substring(1);    //앞의 '/'제거
            //String key = imageUrl.substring(imageUrl.indexOf(bucket) + bucket.length() + 1);
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
            System.out.println("S3 삭제 성공: " + key);
        } catch(S3Exception e){
            System.err.println("S3 삭제 실패 : " + e.awsErrorDetails().errorMessage());
            throw e;
        }
    }


    //채팅 이미지 관련

    //s3업로드 & db 저장
    public ChatImage uploadChatImage(MultipartFile file, Long messageId) throws IOException {
        // S3 업로드 로직
        String imageUrl = uploadBase64Image(convertToBase64(file), "chat");

        // ChatImage 객체 생성 및 저장
        ChatImage chatImage = ChatImage.builder()
                .messageId(messageId)
                .imageUrl(imageUrl)
                .originalName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .build();

        imageMapper.insertChatImage(chatImage);
        return chatImage;
    }

    //messageId로 이미지 찾기
    public List<ChatImage> getChatImagesByMessage(Long messageId) {
        return imageMapper.getChatImagesByMessageId(messageId);
    }

    //chatImageId로 이미지 삭제
    public void deleteChatImage(Long chatImageId) {
        ChatImage chatImage = imageMapper.getChatImageById(chatImageId);
        if (chatImage != null) {
            deleteByUrl(chatImage.getImageUrl()); // 기존 S3 삭제 메서드 활용
            imageMapper.deleteChatImageById(chatImageId);
        }
    }


    // MultipartFile 형식의 이미지를 Base64 문자열로 변환
    public String convertToBase64(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        byte[] bytes = file.getBytes();
        String base64Data = Base64.getEncoder().encodeToString(bytes);

        // "data:image/jpeg;base64," 형태로 반환
        return "data:" + contentType + ";base64," + base64Data;
    }
}
