package com.example.demo.domain.comment.service;

import com.example.demo.domain.comment.model.Comment;
import com.example.demo.domain.user.model.Users;
import com.example.demo.mapper.CommentMapper;
import com.example.demo.mapper.UserMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Date;

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
        comment.setContent(commentContent);
        comment.setUpdatedAt(new Date());

        commentMapper.updateComment(comment);
    }
}
