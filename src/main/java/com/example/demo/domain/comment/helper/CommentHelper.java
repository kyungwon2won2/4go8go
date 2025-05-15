package com.example.demo.domain.comment.helper;

import com.example.demo.domain.comment.model.Comment;
import com.example.demo.mapper.CommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentHelper {
    private final CommentMapper commentMapper;

    public List<Comment> getCommentByPostId(int postId){
        return commentMapper.selectCommentsByPost(postId);
    }
}
