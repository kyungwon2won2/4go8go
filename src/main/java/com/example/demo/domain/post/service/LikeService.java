package com.example.demo.domain.post.service;

import com.example.demo.domain.post.dto.GeneralPostDto;
import com.example.demo.domain.post.model.Like;
import com.example.demo.mapper.LikeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeMapper likeMapper;

    @Transactional
    public boolean toggleLike(int postId, int userId) {
        if (likeMapper.exists(userId, postId)) {
            likeMapper.delete(userId, postId);
            return false;   //좋아요 취소
        } else {
            Like like = new Like();
            like.setUserId(userId);
            like.setPostId(postId);
            likeMapper.insert(like);
            return true;    // 좋아요 성공
        }
    }

    public int getLikeCount(int postId) {
        return likeMapper.countByPostId(postId);
    }

    public boolean hasLiked(int postId, int userId) {
        return likeMapper.exists(userId, postId);
    }

    public List<GeneralPostDto> getLikedPostsPaged(int userId, int offset, int pageSize) {
        return likeMapper.selectLikedPostsByUserId(userId, offset, pageSize);
    }

    public int getLikedPostsCountByUserId(int userId){
        return likeMapper.countLikedPostsByUserId(userId);
    }
}
