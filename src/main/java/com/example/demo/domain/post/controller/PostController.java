package com.example.demo.domain.post.controller;

import com.example.demo.domain.comment.dto.CommentDTO;
import com.example.demo.domain.comment.helper.CommentHelper;
import com.example.demo.domain.post.dto.GeneralDetailDto;
import com.example.demo.domain.post.dto.GeneralPostDto;
import com.example.demo.domain.post.model.Post;
import com.example.demo.domain.post.service.PostService;
import com.example.demo.domain.user.model.CustomerUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/post")
public class PostController {

    private final PostService postService;
    private final CommentHelper commentHelper;

    //전체조회
    @GetMapping
    public String getAllPosts(Model model){
        List<GeneralPostDto> posts = postService.getAllPostsDto();
        System.out.println("Posts : " + posts);
        model.addAttribute("posts", posts);
        return "post/list";
    }

    //상세조회
    @GetMapping("/{postId}")
    public String getPostByIdDto(@PathVariable int postId, Model model, @AuthenticationPrincipal CustomerUser customerUser){
        //조회수 증가
        postService.incrementViewCount(postId);

        //게시글 정보 가져오기
        GeneralDetailDto post = postService.selectPostByIdDto(postId);
        if(post == null){
            return "redirect:/post";
        }
        // 댓글 가져오기 (닉네임 포함)
        List<CommentDTO> commentList = commentHelper.getCommentWithNicknameByPostId(postId);

        model.addAttribute("post", post);
        model.addAttribute("comment_list", commentList);
        model.addAttribute("userId", customerUser.getUserId());
        log.info("customerUser.getUserId(): " + customerUser.getUserId());
        log.info("post.getUserId(): " + post.getUserId());
        return "post/detail";
    }

    //게시글 생성 폼
    @GetMapping("/new")
    public String createPostForm(){
        return "post/form";
    }

    //게시글 생성 처리
    @PostMapping
    public String createPost(@ModelAttribute Post post, @AuthenticationPrincipal CustomerUser loginUser){
        System.out.println(post);
        postService.addPost(post, loginUser.getUserId());
        return "redirect:/post";
    }

    //게시글 수정 폼
    @GetMapping("/{postId}/edit")
    public String updatePostForm(@PathVariable int postId, Model model){
        GeneralDetailDto post = postService.selectPostByIdDto(postId);
        if(post == null){
            return "redirect:/post";
        }
        model.addAttribute("post", post);
        return "/post/edit";
    }

    //게시글 수정 처리
    @PutMapping("/{postId}")
    public String updatePost(@PathVariable int postId, @ModelAttribute Post post){
        post.setPostId(postId);
        postService.updatePost(post);
        return "redirect:/post/{postId}";
    }

    //게시글 삭제 처리
    @PostMapping("/{postId}/delete")
    public String deletePost(@PathVariable int postId){
        postService.deletePost(postId);
        return "redirect:/post";
    }

    //조회수 증가 비동기 요청 처리
    @PostMapping("/{postId}/increment-view-count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> incrementViewCount(@PathVariable int postId){
        //조회수 증가
        postService.incrementViewCount(postId);

        //증가된 조회수 반환
        GeneralDetailDto post = postService.selectPostByIdDto(postId);
        Map<String, Object> response = new HashMap<>();
        response.put("viewCount", post.getViewCount());

        return ResponseEntity.ok(response);
    }


}
