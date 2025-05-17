package com.example.demo.domain.comment.controller;

import com.example.demo.domain.comment.service.CommentService;
import com.example.demo.domain.user.model.CustomerUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
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

    @PutMapping("/{comment_id}")
    public String updateComment(@PathVariable("post_id") int postId, @PathVariable("comment_id") int commentId, String updateComment, @AuthenticationPrincipal CustomerUser user){
        commentService.updateComment(commentId, updateComment, user.getUserId());
        return "redirect:/post/" + postId;
    }
}
