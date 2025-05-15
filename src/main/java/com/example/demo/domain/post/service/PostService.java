package com.example.demo.domain.post.service;

import com.example.demo.domain.post.model.Post;
import com.example.demo.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostMapper postMapper;

    //전체조회
    public List<Post> getAllPosts(){
        return postMapper.selectAllPost();
    }

    //상세조회
    public Post getPostById(int postId){
        System.out.println("Generated postId : " + postId);
        return postMapper.selectPostById(postId);
    }

    //게시글 작성
    public void addPost(Post post){
        post.setCreatedAt(new Date());
        post.setUpdatedAt(new Date());
        postMapper.insertPost(post);

    }

    //게시글 수정
    public void updatePost(Post post){
        post.setUpdatedAt(new Date());
        postMapper.updatePost(post);
    }

    //게시글 삭제
    public void deletePost(int postId){
        postMapper.deletePostById(postId);
    }

    //조회수 증가
    public void incrementViewCount(int postId){
        postMapper.incrementViewCount(postId);
    }





}
