package com.example.demo.domain.post.controller;

import com.example.demo.domain.post.model.Post;
import com.example.demo.domain.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/post")
public class PostController {

    private final PostService postService;

    //전체조회
    @GetMapping
    public String getAllPosts(Model model){
        List<Post> posts = postService.getAllPosts();
        System.out.println("Posts : " + posts);
        model.addAttribute("posts", posts);
        return "post/list";
    }

    //상세조회
    @GetMapping("/{postId}")
    public String getPostById(@PathVariable int postId, Model model){
        //조회수 증가
        postService.incrementViewCount(postId);

        //게시글 정보 가져오기
        Post post = postService.getPostById(postId);
        if(post == null){
            return "redirect:/post";
        }
        model.addAttribute("post", post);
        return "post/detail";
    }

    //게시글 생성 폼
    @GetMapping("/new")
    public String createPostForm(){
        return "post/form";
    }

    //게시글 생성 처리
    @PostMapping
    public String createPost(@ModelAttribute Post post){
        System.out.println(post);
        postService.addPost(post);
        return "redirect:/post";
    }

    //게시글 수정 폼
    @GetMapping("/{postId}/edit")
    public String updatePostForm(@PathVariable int postId, Model model){
        Post post = postService.getPostById(postId);
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
        Post post = postService.getPostById(postId);
        Map<String, Object> response = new HashMap<>();
        response.put("viewCount", post.getViewCount());

        return ResponseEntity.ok(response);
    }


}
