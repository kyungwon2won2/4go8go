package com.example.demo.domain.comment.controller;

import com.example.demo.domain.comment.model.Comment;
import com.example.demo.domain.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/post/{post_id}/comment")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public String createComment(@PathVariable("post_id") int postId, String commentContent, Principal principal){
        commentService.createComment(postId, commentContent, principal);
        System.out.println("젠킨스 테스트");
        return "redirect:/post/" + postId;
    }

    public List<Comment> getCommentList(){
        return null;
    }
}
