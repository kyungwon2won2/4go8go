package com.example.demo.domain.post.service;

import com.example.demo.domain.post.dto.GeneralDetailDto;
import com.example.demo.domain.post.dto.GeneralPostDto;
import com.example.demo.domain.post.model.Image;
import com.example.demo.domain.post.model.Post;
import com.example.demo.mapper.ImageMapper;
import com.example.demo.mapper.PostMapper;
import com.example.demo.util.HtmlImageExtractor;
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
    public void insertPost(Post post, int userId){
        //게시글 저장
        post.setCreatedAt(new Date());
        post.setUpdatedAt(new Date());
        post.setUserId(userId);
        postMapper.insertPost(post);

        List<String> urls = HtmlImageExtractor.extractImageUrls(post.getContent());
        List<Long> imageIds = new ArrayList<>();

        for (String url : urls){
            Image image = new Image();
            image.setPostId(post.getPostId());
            image.setUrl(url);
            imageMapper.insertImage(image);
            imageIds.add(image.getImageId());
        }

        String updatedContent = updateContentWithImageIds(post.getContent(), urls, imageIds);
        post.setContent(updatedContent);
        postMapper.updatePost(post);    //다시 content 업데이트

    }

    //이미지 URL을 image 테이블에 저장하고 id를 반환
    private List<Long> saveImages(List<String> urls, int postId){
        List<Long> imageIds = new ArrayList<>();
        for (String url : urls){
            Image image = new Image();
            image.setPostId(postId);
            image.setUrl(url);
            System.out.println("삽입할 이미지 URL : " + url + ", postId: " + postId);
            // Image 테이블 저장
            imageMapper.insertImage(image);
            //image ID 저장
            imageIds.add(image.getImageId());
        }
        return imageIds;
    }

    //이미지 ID로 content 변환 (기존 img 태그를 imageId로 변환)
    private String updateContentWithImageIds(String content, List<String> urls, List<Long> imageIds) {
        // 예시: <img src="...">를 <img data-id="imageId">로 변환
        for (int i = 0; i < urls.size(); i++){
            String src = urls.get(i);
            Long id = imageIds.get(i);
            //src가 정확히 일치하는 <img> 태그만 찾아서 대체
            content = content.replaceFirst(
                    "<img[^>]*src=[\"']" + Pattern.quote(src) + "[\"'][^>]*>",
                    "<img data-id=\"" + id + "\">"
            );
        }
        return content;
    }

    //게시글 수정
    @Transactional
    public void updatePost(Post post){
        post.setUpdatedAt(new Date());
        postMapper.updatePost(post);

        imageMapper.deleteImagesByPostId(post.getPostId());

        List<String> urls = HtmlImageExtractor.extractImageUrls(post.getContent());
        for (String url : urls){
            Image image = new Image();
            image.setPostId(post.getPostId());
            image.setUrl(url);
            imageMapper.insertImage(image);
        }
    }

    //게시글 삭제
    @Transactional
    public void deletePostById(int postId){
        imageMapper.deleteImagesByPostId(postId);
        postMapper.deletePostById(postId);
    }

    //조회수 증가
    public void incrementViewCount(int postId){
        postMapper.incrementViewCount(postId);
    }





}
