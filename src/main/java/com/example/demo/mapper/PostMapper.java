package com.example.demo.mapper;

import com.example.demo.domain.post.model.Post;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PostMapper {

    //게시글 작성
    void insertPost(Post post);

    //게시글 전체조회
    List<Post> selectAllPost();

    //게시글 단일조회
    Post selectPostById(int postId);

    //게시글 수정
    void updatePost(Post post);

    //게시글 삭제
    void deletePostById(int postId);

    // 조회수 증가
    void incrementViewCount(int postId);
}
