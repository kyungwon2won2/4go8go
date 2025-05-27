package com.example.demo.domain.post.controller;

import com.example.demo.domain.post.service.LikeService;
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
@RequestMapping("/like")
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/toggle")
    public Map<String, Object> toggleLike(
            @RequestParam int postId,
            @AuthenticationPrincipal CustomerUser loginUser) {

        if (loginUser == null) {
            throw new RuntimeException("로그인이 필요합니다.");
        }

        boolean liked = likeService.toggleLike(postId, loginUser.getUserId());
        int likeCount = likeService.getLikeCount(postId);

        return Map.of(
                "liked", liked,
                "count", likeCount
        );
    }
}
