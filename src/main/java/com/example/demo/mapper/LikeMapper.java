package com.example.demo.mapper;

import com.example.demo.domain.post.model.Like;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LikeMapper {
    void insert(Like like);

    void delete(@Param("userId") int userId,
                @Param("postId") int postId);

    boolean exists(@Param("userId") int userId,
                   @Param("postId") int postId);

    int countByPostId(@Param("postId") int postId);
}
