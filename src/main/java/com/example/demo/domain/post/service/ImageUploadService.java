package com.example.demo.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageUploadService {

    private final S3Client s3Client;

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
}
