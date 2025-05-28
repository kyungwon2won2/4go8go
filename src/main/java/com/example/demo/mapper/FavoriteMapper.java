package com.example.demo.mapper;

import com.example.demo.domain.post.dto.GeneralPostDto;
import com.example.demo.domain.post.dto.ProductListDto;
import com.example.demo.domain.post.model.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FavoriteMapper {
    void insert(Favorite favorite);

    void delete(@Param("userId") int userId,
                @Param("postId") int postId);

    boolean exists(@Param("userId") int userId,
                   @Param("postId") int postId);

    int countByPostId(@Param("postId") int postId);

    List<ProductListDto> selectFavoritedProductsByUserId(@Param("userId") int userId,
                                                        @Param("offset") int offset,
                                                        @Param("pageSize") int pageSize);

    int countFavoritedProductsByUserId(@Param("userId") int userId);
}
