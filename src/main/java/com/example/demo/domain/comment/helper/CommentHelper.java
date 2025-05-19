package com.example.demo.domain.comment.helper;

import com.example.demo.domain.comment.dto.CommentDTO;
import com.example.demo.domain.comment.model.Comment;
import com.example.demo.mapper.CommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentHelper {
    private final CommentMapper commentMapper;

    // 기본 댓글 목록 (닉네임 없음)
    public List<Comment> getCommentByPostId(int postId){
        return commentMapper.selectCommentsByPost(postId);
    }
    
    // 닉네임이 포함된 댓글 목록
    public List<CommentDTO> getCommentWithNicknameByPostId(int postId){
        return commentMapper.selectCommentsByPostWithNickname(postId);
    }
}
