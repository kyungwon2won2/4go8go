package com.example.demo.mapper;

import com.example.demo.domain.post.dto.GeneralDetailDto;
import com.example.demo.domain.post.dto.GeneralPostDto;
import com.example.demo.domain.post.model.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
    Post selectPostById(int postId);

    //게시글 수정
    void updatePost(Post post);

    //게시글 삭제
    void deletePostById(int postId);

    void incrementViewCount(int postId);

    void updatePostContentAndTitle(@Param("postId") int postId,
                                   @Param("title") String title,
                                   @Param("content") String content);

    // 페이징 목록 조회
    List<GeneralPostDto> selectPostsByPage(@Param("offset") int offset, @Param("limit") int limit);

    // 전체 게시글 수
    int countAllPosts();
}

