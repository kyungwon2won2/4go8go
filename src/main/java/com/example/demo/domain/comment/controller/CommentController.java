package com.example.demo.domain.comment.controller;

import com.example.demo.domain.comment.dto.CreateCommentRequest;
import com.example.demo.domain.comment.dto.UpdateCommentRequest;
import com.example.demo.domain.comment.service.CommentService;
import com.example.demo.domain.user.model.CustomerUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping()
public class CommentController {

    private final CommentService commentService;
    
    // 댓글 생성 API
    @PostMapping("/user/post/{postId}/comment")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createCommentApi(
            @PathVariable("postId") int postId,
            @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal CustomerUser loginUser) {
        return ResponseEntity.ok(commentService.createComment(postId, request.commentContent(), loginUser));
    }

    // 댓글 목록 조회 API
    @GetMapping("/post/{postId}/comments")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCommentsApi(
            @PathVariable("postId") int postId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Map<String, Object> result = commentService.getCommentsWithTotalByPostWithNickname(postId, page, size);
        return ResponseEntity.ok(result);
    }

    // 댓글 수정 API
    @PutMapping("/user/comment/{commentId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCommentApi(
            @PathVariable("commentId") int commentId,
            @RequestBody UpdateCommentRequest request,
            @AuthenticationPrincipal CustomerUser user) {
        return ResponseEntity.ok(commentService.updateComment(commentId, request.commentContent(), user.getUserId()));
    }

    // 댓글 삭제 API
    @DeleteMapping("/user/comment/{commentId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteCommentApi(
            @PathVariable("commentId") int commentId,
            @AuthenticationPrincipal CustomerUser user) {
        System.out.println("jenkins");
        return ResponseEntity.ok(commentService.deleteComment(commentId, user.getUserId()));
    }
}
