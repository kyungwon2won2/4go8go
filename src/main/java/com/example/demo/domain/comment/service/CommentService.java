package com.example.demo.domain.comment.service;

import com.example.demo.domain.comment.dto.CommentDTO;
import com.example.demo.domain.comment.model.Comment;
import com.example.demo.domain.user.model.Users;
import com.example.demo.mapper.CommentMapper;
import com.example.demo.mapper.UserMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentMapper commentMapper;
    private final UserMapper userMapper;

    @Transactional
    public void createComment(int postId, String commentContent, Principal principal){
        Users user = userMapper.getUserById(principal.getName());
        Comment comment = new Comment(
                postId,
                user.getUserId(),
                commentContent,
                new Date()    // createdAt
        );

        commentMapper.insertComment(comment);
    }

    @Transactional
    public void updateComment(int commentId, String commentContent, int userId){
        Comment comment = commentMapper.selectComment(commentId);
        if (comment.getUserId() == userId) {
            comment.setContent(commentContent);
            comment.setUpdatedAt(new Date());

            commentMapper.updateComment(comment);
        }

    }
    
    @Transactional
    public void deleteComment(int commentId, int userId) {
        Comment comment = commentMapper.selectComment(commentId);
        if (comment.getUserId() == userId) {
            commentMapper.deleteComment(commentId);
        }
    }
    
    // 댓글 1개 상세 조회 (닉네임 포함)
    public CommentDTO getCommentWithNickname(int commentId) {
        return commentMapper.selectCommentWithNickname(commentId);
    }
    
    // 게시글에 달린 댓글 목록 조회 (닉네임 포함)
    public List<CommentDTO> getCommentsByPostWithNickname(int postId) {
        return commentMapper.selectCommentsByPostWithNickname(postId);
    }
}
