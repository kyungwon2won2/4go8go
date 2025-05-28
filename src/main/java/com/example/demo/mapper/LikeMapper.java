package com.example.demo.mapper;

import com.example.demo.domain.post.dto.GeneralPostDto;
import com.example.demo.domain.post.model.Like;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LikeMapper {
    void insert(Like like);

    void delete(@Param("userId") int userId,
                @Param("postId") int postId);

    boolean exists(@Param("userId") int userId,
                   @Param("postId") int postId);

    int countByPostId(@Param("postId") int postId);

    //좋아요 누른 게시글 목록 조회(페이징)
    List<GeneralPostDto> selectLikedPostsByUserId(@Param("userId") int userId,
                                                  @Param("offset") int offset,
                                                  @Param("pageSize") int pageSize);

    // 좋아요 누른 게시글 총 개수 조회
    int countLikedPostsByUserId(@Param("userId") int userId);
}
