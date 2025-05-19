package com.example.demo.domain.post.service;

import com.example.demo.domain.post.dto.GeneralDetailDto;
import com.example.demo.domain.post.dto.GeneralPostDto;
import com.example.demo.domain.post.model.Image;
import com.example.demo.domain.post.model.Post;
import com.example.demo.mapper.ImageMapper;
import com.example.demo.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostMapper postMapper;
    private final ImageMapper imageMapper;

    //전체조회
    public List<GeneralPostDto> getAllPostsDto(){
        return postMapper.selectAllPostsDto();
    }

    //상세조회
    public GeneralDetailDto selectPostByIdDto(int postId){
        System.out.println("Generated postId : " + postId);
        return postMapper.selectPostByIdDto(postId);
    }

    //게시글 작성
    @Transactional
    public void addPost(Post post, int userId){
        post.setCreatedAt(new Date());
        post.setUpdatedAt(new Date());
        post.setUserId(userId);

        // 게시글 저장 (postId 생성됨)
        postMapper.insertPost(post);

        // 이미지 추출 및 저장
        String content = post.getContent();
        List<String> imageUrls = extractImageUrls(content);

        // 이미지 테이블에 postId 포함 저장
        List<Long> imageIds = saveImages(imageUrls, post.getPostId());

        // content 내부 <img> 태그를 id 기반으로 변환
        post.setContent(updateContentWithImageIds(content, imageIds));
        postMapper.updatePost(post);

    }

    // 이미지를 포함한 URL을 content에서 추출하는 메서드
    private List<String> extractImageUrls(String content){
        List<String> imageUrls = new ArrayList<>();
        Pattern pattern = Pattern.compile("<img[^>]+src=\"(http[^\"]+)\"[^>]*>");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()){
            imageUrls.add(matcher.group(1));    //이미지 src 추출
        }
        return imageUrls;
    }

    //이미지 URL을 image 테이블에 저장하고 id를 반환
    private List<Long> saveImages(List<String> imageUrls, int postId){
        List<Long> imageIds = new ArrayList<>();
        for (String imageUrl : imageUrls){
            Image image = new Image();
            image.setUrl(imageUrl);
            image.setPostId(postId);
            // Image 테이블 저장
            imageMapper.insertImage(image);
            //image ID 저장
            imageIds.add(image.getImageId());
        }
        return imageIds;
    }

    //이미지 ID로 content 변환 (기존 img 태그를 imageId로 변환)
    private String updateContentWithImageIds(String content, List<Long> imageIds) {
        // 예시: <img src="...">를 <img data-id="imageId">로 변환
        for (int i = 0; i < imageIds.size(); i++) {
            String imageTag = "<img src=\"[SRC]\">";
            content = content.replaceFirst(imageTag, "<img data-id=\"" + imageIds.get(i) + "\">");
        }
        return content;
    }

    //게시글 수정
    @Transactional
    public void updatePost(Post post){
        post.setUpdatedAt(new Date());
        postMapper.updatePost(post);
    }

    //게시글 삭제
    @Transactional
    public void deletePost(int postId){
        postMapper.deletePostById(postId);
    }

    //조회수 증가
    public void incrementViewCount(int postId){
        postMapper.incrementViewCount(postId);
    }





}
