package com.example.demo.domain.comment.controller;

import com.example.demo.domain.comment.dto.CommentDTO;
import com.example.demo.domain.comment.service.CommentService;
import com.example.demo.domain.user.model.CustomerUser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping()
public class CommentController {

    private final CommentService commentService;
    
    // 댓글 생성 API
    @PostMapping("/post/{postId}/comment")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createCommentApi(
            @PathVariable("postId") int postId,
            @RequestBody String rawPayload,
            Principal principal) {
        try {
            // JSON 문자열을 Map으로 변환
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> payload = objectMapper.readValue(rawPayload, new TypeReference<Map<String, String>>() {});
            
            System.out.println("변환된 페이로드: " + payload);
            
            String commentContent = payload.get("commentContent");
            System.out.println("댓글 내용: " + commentContent);
            
            if (commentContent == null || commentContent.trim().isEmpty()) {
                throw new IllegalArgumentException("댓글 내용이 비어있습니다.");
            }
            
            if (principal == null) {
                throw new IllegalStateException("로그인이 필요합니다.");
            }
            
            commentService.createComment(postId, commentContent, principal);
            
            // 새로 추가된 댓글 정보 조회
            List<CommentDTO> comments = commentService.getCommentsByPostWithNickname(postId);
            CommentDTO newComment = comments.get(comments.size() - 1);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("comment", newComment);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("댓글 생성 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 댓글 목록 조회 API
    @GetMapping("/post/{postId}/comments")
    @ResponseBody
    public ResponseEntity<List<CommentDTO>> getCommentsApi(@PathVariable("postId") int postId) {
        List<CommentDTO> comments = commentService.getCommentsByPostWithNickname(postId);
        return ResponseEntity.ok(comments);
    }

    // 댓글 수정 API
    @PutMapping("/comment/{commentId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCommentApi(
            @PathVariable("commentId") int commentId,
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal CustomerUser user) {
        
        String commentContent = payload.get("commentContent");
        commentService.updateComment(commentId, commentContent, user.getUserId());
        
        // 수정된 댓글 정보 조회
        CommentDTO updatedComment = commentService.getCommentWithNickname(commentId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("comment", updatedComment);
        
        return ResponseEntity.ok(response);
    }

    // 댓글 삭제 API
    @DeleteMapping("/comment/{commentId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteCommentApi(
            @PathVariable("commentId") int commentId,
            @AuthenticationPrincipal CustomerUser user) {
        
        commentService.deleteComment(commentId, user.getUserId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        
        return ResponseEntity.ok(response);
    }
}
