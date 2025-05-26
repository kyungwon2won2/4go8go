package com.example.demo.domain.review.controller;

import com.example.demo.domain.review.dto.CreateReviewDto;
import com.example.demo.domain.review.dto.ReviewListDto;
import com.example.demo.domain.review.dto.UserReviewSummaryDto;
import com.example.demo.domain.review.service.ReviewService;
import com.example.demo.domain.user.model.CustomerUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/review")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    // 리뷰 작성 폼
    @GetMapping("/create/{postId}")
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
    @PostMapping("/create")
    public String createReview(@ModelAttribute CreateReviewDto dto,
                               @AuthenticationPrincipal CustomerUser user,
                               Model model) {
        try {
            int reviewerId = user.getUserId();
            reviewService.createReview(dto, reviewerId);
            return "redirect:/post/" + dto.getPostId() + "?success=review_created";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("postId", dto.getPostId());
            return "review/create";
        }
    }

    // 사용자별 리뷰 목록 + 페이징
    @GetMapping("/user/{userId}")
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