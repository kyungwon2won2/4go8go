package com.example.demo.mapper;

import com.example.demo.domain.comment.model.Comment;

import java.util.List;

public interface CommentMapper {

    // 댓글 등록
    void insertComment(Comment comment);

    // 댓글 1개 조회
    Comment selectComment(int commentId);

    // 게시글에 달린 댓글 목록 조회
    List<Comment> selectCommentsByPost(int postId);

    // 댓글 수정
    void updateComment(Comment comment);

    // 댓글 삭제 (소프트 삭제)
    void deleteComment(int commentId);
}
