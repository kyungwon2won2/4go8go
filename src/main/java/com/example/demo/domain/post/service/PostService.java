package com.example.demo.domain.post.service;

import com.example.demo.domain.post.dto.GeneralDetailDto;
import com.example.demo.domain.post.dto.GeneralPostDto;
import com.example.demo.domain.post.model.Image;
import com.example.demo.domain.post.model.Post;
import com.example.demo.mapper.CommentMapper;
import com.example.demo.mapper.ImageMapper;
import com.example.demo.mapper.PostMapper;
import com.example.demo.util.HtmlImageExtractor;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    private final ImageUploadService imageUploadService;
    private final CommentMapper commentMapper;

    //전체조회
    public List<GeneralPostDto> getAllPostsDto(){
        return postMapper.selectAllPostsDto();
    }

    //상세조회
    public GeneralDetailDto selectPostByIdDto(int postId){
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

        List<String> base64List = HtmlImageExtractor.extractImageUrls(post.getContent());
        List<String> originalList = new ArrayList<>();
        List<Long> imageIds = new ArrayList<>();

        for (String base64 : base64List){
            try{
                String s3Url = imageUploadService.uploadBase64Image(base64, "post-images");

                Image image = new Image();
                image.setPostId(post.getPostId());
                image.setUrl(s3Url);
                imageMapper.insertImage(image);

                originalList.add(base64);
                imageIds.add(image.getImageId());

            } catch(Exception e){
                //실패한 이미지 건너뛰거나, 전체 롤백 여부 판단
                throw new RuntimeException("이미지 업로드 실패: " + e.getMessage(), e);
            }

        }

        String updatedContent = updateContentWithImageIds(post.getContent(), originalList, imageIds);
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
        for (int i = 0; i < imageIds.size(); i++){
            content = content.replaceFirst(
                    "<img[^>]*src=[\"']" + Pattern.quote(urls.get(i)) + "[\"'][^>]*>",
                    "<img data-id=\"" + imageIds.get(i) + "\">"
            );
        }
        return content;
    }

    //게시글 수정
    @Transactional
    public void updatePost(Post post){

        post.setUpdatedAt(new Date());

        // 이미지 일부 수정 시
        // 1. HTML 파싱 : 기존 <img data-id="...">와 새 <img data-id="..."> 분리
        Document doc = Jsoup.parse(post.getContent());
        Elements imgs = doc.select("img");

        List<Long> usedImageIds = new ArrayList<>();    //data-id로 유지될 기존 이미지
        List<String> base64Images = new ArrayList<>();  // 새로 추가된 base64 이미지
        List<String> originalBase64List = new ArrayList<>();
        List<Long> newImageIds = new ArrayList<>();

        for (Element img : imgs){
            String dataId = img.attr("data-id");
            String src = img.attr("src");

            if(dataId != null && !dataId.isBlank()){
                try{
                    usedImageIds.add(Long.parseLong(dataId));
                } catch(NumberFormatException e){
                    // 잘못된 data-id 무시
                }
            } else if (src != null && src.startsWith("data:image")){
                base64Images.add(src);
            }
        }

        // 2. 기존 이미지 중 사용되지 않은 것만 S3 + DB에서 삭제
        List<Image> existingImages = imageMapper.getImagesByPostId(post.getPostId());
        for(Image image : existingImages) {
            if (!usedImageIds.contains(image.getImageId())){
                imageUploadService.deleteByUrl(image.getUrl());
                imageMapper.deleteImageById(image.getImageId());
            }
        }

        // 3. 새 base64 이미지업로드 후 image 테이블 저장
        for (String base64 : base64Images){
            try{
                String s3Url = imageUploadService.uploadBase64Image(base64, "post-images");

                Image image = new Image();
                image.setPostId(post.getPostId());
                image.setUrl(s3Url);
                imageMapper.insertImage(image);

                originalBase64List.add(base64);
                newImageIds.add(image.getImageId());
            } catch (IOException e){
                throw new RuntimeException("이미지 업로드 실패", e);
            }
        }

        // 4. content에서 base64 src를 <img data-id="..."> 로 변환
        String updatedContent = updateContentWithImageIds(post.getContent(), originalBase64List, newImageIds);
        post.setContent(updatedContent);

        // 5. 최종 DB 업데이트
        postMapper.updatePost(post);
    }

    //게시글 삭제
    @Transactional
    public void deletePostById(int postId){
        // 1. 댓글 삭제
        commentMapper.deleteByPostId(postId);

        // 2. 이미지 S3 삭제 + DB 삭제
        List<Image> images = imageMapper.getImagesByPostId(postId);
        for (Image image : images){
            imageUploadService.deleteByUrl(image.getUrl());
        }
        imageMapper.deleteImagesByPostId(postId);

        // 2. 게시글 삭제
        postMapper.deletePostById(postId);
    }

    //조회수 증가
    public void incrementViewCount(int postId){
        postMapper.incrementViewCount(postId);
    }

    // 페이징
    public List<GeneralPostDto> getPostsByPage(int page, int pageSize){
        int offset = (page - 1) * pageSize;
        return postMapper.selectPostsByPage(offset, pageSize);
    }

    // 일반게시판 전체 글 갯수
    public int getTotalPostCount(){
        return postMapper.countAllPosts();
    }




}
