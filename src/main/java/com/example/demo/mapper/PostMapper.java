package com.example.demo.mapper;

import com.example.demo.domain.post.DTO.GeneralDetailDto;
import com.example.demo.domain.post.DTO.GeneralPostDto;
import com.example.demo.domain.post.model.Post;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PostMapper {

    //게시글 작성
    void insertPost(Post post);

    //게시글 전체조회 ? 다시 확인할 것
    //List<Post> selectAllPost();

    //일반게시판 전체조회(DTO)
    List<GeneralPostDto> selectAllPostsDto();

    //일반게시판 상세조회(DTO)
    GeneralDetailDto selectPostByIdDto(int postId);

    //게시글 단일조회
    //Post selectPostById(int postId);

    //게시글 수정
    void updatePost(Post post);

    //게시글 삭제
    void deletePostById(int postId);

    void incrementViewCount(int postId);
}

