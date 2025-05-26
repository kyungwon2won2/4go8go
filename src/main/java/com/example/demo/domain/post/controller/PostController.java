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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.AccessDeniedException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping
public class PostController {

    private final PostService postService;
    private final CommentHelper commentHelper;

    //전체조회
    @GetMapping("/post")
    public String getAllPosts(@RequestParam(defaultValue = "1") int page, Model model){
        int pageSize = 10;

        //List<GeneralPostDto> posts = postService.getAllPostsDto();
        List<GeneralPostDto> posts = postService.getPostsByPage(page, pageSize);
        int totalPosts = postService.getTotalPostCount();
        int totalPages = (int) Math.ceil((double) totalPosts / pageSize);

        model.addAttribute("posts", posts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        return "post/list";
    }

    //상세조회
    @GetMapping("/post/{postId}")
    public String getPostByIdDto(@PathVariable int postId, Model model, @AuthenticationPrincipal CustomerUser customerUser){
        //조회수 증가
        postService.incrementViewCount(postId);

        //게시글 정보 가져오기
        GeneralDetailDto post = postService.selectPostByIdDto(postId);
        if(post == null){
            return "redirect:/post";
        }
        
        // 비로그인 사용자 처리
        Integer currentUserId = null;
        boolean isOwner = false;
        boolean isAdmin = false;

        if(customerUser != null){
            currentUserId = customerUser.getUserId();
            isOwner = (currentUserId == post.getUserId());
            isAdmin = customerUser.hasRole("ROLE_ADMIN");
        }

        // 댓글 가져오기 (닉네임 포함)
        List<CommentDTO> commentList = commentHelper.getCommentWithNicknameByPostId(postId);

        model.addAttribute("post", post);
        model.addAttribute("comment_list", commentList);
        model.addAttribute("userId", currentUserId);
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("isAdmin", isAdmin);
        log.info("post.getUserId(): " + post.getUserId());
        return "post/detail";
    }

    //게시글 생성 폼
    @GetMapping("/user/post/new")
    public String createPostForm(){
        return "post/form";
    }

    //게시글 생성 처리
    @PostMapping("/user/post")
    public String createPost(@ModelAttribute Post post, @AuthenticationPrincipal CustomerUser loginUser){
        postService.insertPost(post, loginUser.getUserId());
        return "redirect:/post";
    }

    //게시글 수정 폼
    @GetMapping("/user/post/{postId}/edit")
    public String updatePostForm(@PathVariable int postId, Model model, @AuthenticationPrincipal CustomerUser customerUser){
        GeneralDetailDto post = postService.selectPostByIdDto(postId);
        if(post == null){
            return "redirect:/post";
        }
        model.addAttribute("post", post);
        return "/post/edit";
    }

    //게시글 수정 처리
    @PutMapping("/user/post/{postId}")
    public String updatePost(@PathVariable int postId, @ModelAttribute Post post, @AuthenticationPrincipal CustomerUser customerUser){
        //권한 체크
        GeneralDetailDto generalDetailDto = postService.selectPostByIdDto(postId);
        if(generalDetailDto.getUserId() != customerUser.getUserId()){
            throw new AccessDeniedException("작성자만 수정할 수 있습니다.");
        }
        post.setPostId(postId);
        postService.updatePost(post);
        return "redirect:/post/{postId}";
    }

    //게시글 삭제 처리
    @PostMapping("/user/post/{postId}/delete")
    public String deletePost(@PathVariable int postId, @AuthenticationPrincipal CustomerUser customerUser, Model model){
        GeneralDetailDto generalDetailDto = postService.selectPostByIdDto(postId);

        boolean isOwner = generalDetailDto.getUserId() == customerUser.getUserId();
        boolean isAdmin = customerUser.hasRole("ROLE_ADMIN");

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("작성자 또는 관리자만 삭제할 수 있습니다.");
        }

        postService.deletePostById(postId);
        return "redirect:/post";
    }

    //조회수 증가 비동기 요청 처리
    @PostMapping("/post/{postId}/increment-view-count")
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
