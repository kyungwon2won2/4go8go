package com.example.demo.domain.review.controller;

import com.example.demo.domain.review.dto.CreateReviewDto;
import com.example.demo.domain.review.dto.ReviewListDto;
import com.example.demo.domain.review.dto.UserReviewSummaryDto;
import com.example.demo.domain.review.service.ReviewService;
import com.example.demo.domain.user.model.CustomerUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    // 리뷰 작성 폼
    @GetMapping("/user/review/create/{postId}")
    public String createReviewForm(@PathVariable int postId, Model model, @AuthenticationPrincipal CustomerUser user) {
        int userId = user.getUserId();

        // 리뷰 작성 가능 여부 확인
        if (!reviewService.canWriteReview(postId, userId)) {
            return "redirect:/post/" + postId + "?error=already_reviewed";
        }

        model.addAttribute("postId", postId);
        return "review/create";
    }

    // 리뷰 작성 처리
    @PostMapping("/user/review/create/api")
    @ResponseBody
    public ResponseEntity<?> createReviewApi(@RequestBody CreateReviewDto dto,
                                             @AuthenticationPrincipal CustomerUser user) {
        try {
            int reviewerId = user.getUserId();
            reviewService.createReview(dto, reviewerId);
            return ResponseEntity.ok(Map.of("success", true, "message", "리뷰가 작성되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // 사용자별 리뷰 목록 + 페이징
    @GetMapping("/user/review/{userId}")
    public String getUserReviews(@PathVariable int userId,
                                 @RequestParam(defaultValue = "1") int page,
                                 Model model) {
        List<ReviewListDto> reviews = reviewService.getReviewsByUser(userId, page, 10);
        UserReviewSummaryDto summary = reviewService.getUserReviewSummary(userId);

        model.addAttribute("reviews", reviews);
        model.addAttribute("summary", summary);
        model.addAttribute("currentPage", page);
        return "review/user-reviews";
    }
}