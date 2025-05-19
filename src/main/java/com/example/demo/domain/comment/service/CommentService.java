package com.example.demo.domain.comment.service;

import com.example.demo.domain.comment.dto.CommentDTO;
import com.example.demo.domain.comment.model.Comment;
import com.example.demo.domain.user.model.CustomerUser;
import com.example.demo.mapper.CommentMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentMapper commentMapper;

    @Transactional
    public Map<String, Object> createComment(int postId, String commentContent, CustomerUser loginUser){
        if (loginUser == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        if (commentContent == null || commentContent.trim().isEmpty()) {
            throw new IllegalArgumentException("댓글 내용이 비어있습니다.");
        }
        Comment comment = new Comment(
                postId,
                loginUser.getUserId(),
                commentContent,
                new Date()    // createdAt
        );
        commentMapper.insertComment(comment);
        List<CommentDTO> comments = commentMapper.selectCommentsByPostWithNickname(postId, 0, Integer.MAX_VALUE);
        CommentDTO newComment = comments.get(comments.size() - 1);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("comment", newComment);
        return response;
    }

    @Transactional
    public Map<String, Object> updateComment(int commentId, String commentContent, int userId){
        if (commentContent == null || commentContent.trim().isEmpty()) {
            throw new IllegalArgumentException("댓글 내용이 비어있습니다.");
        }
        Comment comment = commentMapper.selectComment(commentId);
        if (comment.getUserId() == userId) {
            comment.setContent(commentContent);
            comment.setUpdatedAt(new Date());
            commentMapper.updateComment(comment);
        } else {
            throw new IllegalStateException("댓글 수정 권한이 없습니다.");
        }
        CommentDTO updatedComment = commentMapper.selectCommentWithNickname(commentId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("comment", updatedComment);
        return response;
    }
    
    @Transactional
    public Map<String, Object> deleteComment(int commentId, int userId) {
        Comment comment = commentMapper.selectComment(commentId);
        if (comment.getUserId() == userId) {
            commentMapper.deleteComment(commentId);
        } else {
            throw new IllegalStateException("댓글 삭제 권한이 없습니다.");
        }
        // 삭제 후 남은 댓글 목록과 전체 개수 반환
        List<CommentDTO> comments = commentMapper.selectCommentsByPostWithNickname(comment.getPostId(), 0, Integer.MAX_VALUE);
        int totalCount = commentMapper.selectCommentCountByPostId(comment.getPostId());
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("comments", comments);
        response.put("totalCount", totalCount);
        return response;
    }

    // 댓글 1개 상세 조회 (닉네임 포함)
    public CommentDTO getCommentWithNickname(int commentId) {
        return commentMapper.selectCommentWithNickname(commentId);
    }
    
    // 게시글에 달린 댓글 목록 조회 (닉네임 포함, 페이징)
    public List<CommentDTO> getCommentsByPostWithNickname(int postId, int page, int size) {
        int offset = (page - 1) * size;
        return commentMapper.selectCommentsByPostWithNickname(postId, offset, size);
    }
    // (기존) 전체 목록 조회는 deprecated 또는 유지
    @Deprecated
    public List<CommentDTO> getCommentsByPostWithNickname(int postId) {
        return commentMapper.selectCommentsByPostWithNickname(postId, 0, Integer.MAX_VALUE);
    }

    // 게시글에 달린 댓글 목록(페이징) + 전체 개수 반환
    public Map<String, Object> getCommentsWithTotalByPostWithNickname(int postId, int page, int size) {
        int offset = (page - 1) * size;
        List<CommentDTO> comments = commentMapper.selectCommentsByPostWithNickname(postId, offset, size);
        int totalCount = commentMapper.selectCommentCountByPostId(postId);
        Map<String, Object> result = new HashMap<>();
        result.put("comments", comments);
        result.put("totalCount", totalCount);
        return result;
    }
}
