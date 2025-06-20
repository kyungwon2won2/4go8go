package com.example.demo.domain.post.service;

import com.example.demo.domain.post.dto.GeneralPostDto;
import com.example.demo.domain.post.dto.ProductListDto;
import com.example.demo.domain.post.model.Favorite;
import com.example.demo.mapper.FavoriteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteMapper favoriteMapper;

    @Transactional
    public boolean toggleFavorite(int postId, int userId){
        if(favoriteMapper.exists(userId, postId)){
            favoriteMapper.delete(userId, postId);
            return false;   // 찜 취소
        } else {
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setPostId(postId);
            favoriteMapper.insert(favorite);
            return true;    // 찜 추가
        }
    }

    public int getFavoriteCount(int postId){
        return favoriteMapper.countByPostId(postId);
    }

    public boolean hasFavorited(int postId, int userId){
        return favoriteMapper.exists(userId, postId);
    }

    public int getFavoritedProductsCountByUserId(int userId){
        return favoriteMapper.countFavoritedProductsByUserId(userId);
    }

    public List<ProductListDto> getFavoritedProductsPaged(int userId, int offset, int pageSize) {
        return favoriteMapper.selectFavoritedProductsByUserId(userId, offset, pageSize);
    }
}
