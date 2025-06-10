package com.example.demo.domain.post.controller;

import com.example.demo.common.exception.custom.CustomException;
import com.example.demo.common.stringcode.ErrorCode;
import com.example.demo.domain.post.dto.GeneralPostDto;
import com.example.demo.domain.post.service.LikeService;
import com.example.demo.domain.user.model.CustomerUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/like")
@Slf4j
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/toggle")
    @ResponseBody
    public ResponseEntity<?> toggleLike(
            @RequestParam int postId,
            @AuthenticationPrincipal CustomerUser loginUser) {

        log.info("toggleLike 호출 - postId: {}, userId: {}", postId, loginUser != null ? loginUser.getUserId() : "비로그인");

        if (loginUser == null) {
            log.warn("로그인 필요 - 로그인 사용자 정보가 없음");
            throw new CustomException(ErrorCode.LOGIN_REQUIRED);
        }

        boolean liked;
        try{
            liked = likeService.toggleLike(postId, loginUser.getUserId());
        } catch(Exception e){
            log.error("likeService.toggleLike 처리 중 오류", e);
            throw e;
        }
        int likeCount = likeService.getLikeCount(postId);

        return ResponseEntity.ok(Map.of("liked", liked, "count", likeCount));
    }

    @GetMapping("/likedList")
    public String likedPosts(@RequestParam(defaultValue = "1") int page,
                             Model model,
                             @AuthenticationPrincipal CustomerUser loginUser){
        if (loginUser == null){
            return "redirect:/login";
        }

        int pageSize = 20;
        int offset = (page - 1) * pageSize;

        List<GeneralPostDto> likedPosts = likeService.getLikedPostsPaged(loginUser.getUserId(), offset, pageSize);
        int totalCount = likeService.getLikedPostsCountByUserId(loginUser.getUserId());

        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        boolean hasNext = page < totalPages;
        boolean hasPrev = page > 1;

        model.addAttribute("posts", likedPosts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("hasNext", hasNext);
        model.addAttribute("hasPrev", hasPrev);
        model.addAttribute("categoryDisplayName","내가 좋아요한 게시글");

        return "post/myLikedList";
    }
}
