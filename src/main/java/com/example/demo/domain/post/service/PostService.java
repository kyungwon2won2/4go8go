package com.example.demo.domain.post.service;

import com.example.demo.domain.post.dto.GeneralDetailDto;
import com.example.demo.domain.post.dto.GeneralPostDto;
import com.example.demo.domain.post.model.Post;
import com.example.demo.domain.user.model.Users;
import com.example.demo.mapper.PostMapper;
import com.example.demo.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostMapper postMapper;
    //private final UserMapper userMapper;

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

        //Users user = userMapper.getUserById(principal.getName());

        post.setCreatedAt(new Date());
        post.setUpdatedAt(new Date());
        post.setUserId(userId);
        postMapper.insertPost(post);

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
