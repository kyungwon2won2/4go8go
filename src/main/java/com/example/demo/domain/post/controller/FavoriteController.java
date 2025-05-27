package com.example.demo.domain.post.controller;

import com.example.demo.domain.post.service.FavoriteService;
import com.example.demo.domain.user.model.CustomerUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/favorite")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/toggle")
    public Map<String, Object> toggleFavorite(
            @RequestParam int postId,
            @AuthenticationPrincipal CustomerUser loginUser){

        if (loginUser == null) {
            throw new RuntimeException("로그인이 필요합니다.");
        }

        boolean favorited = favoriteService.toggleFavorite(postId, loginUser.getUserId());
        int favoriteCount = favoriteService.getFavoriteCount(postId);

        return Map.of(
                "favorited", favorited,
                "count", favoriteCount
        );
    }
}
