package com.example.demo.domain.review.service;

import com.example.demo.domain.post.model.Post;
import com.example.demo.domain.post.model.Product;
import com.example.demo.domain.review.dto.CreateReviewDto;
import com.example.demo.domain.review.dto.ReviewListDto;
import com.example.demo.domain.review.dto.UserReviewSummaryDto;
import com.example.demo.domain.review.model.Review;
import com.example.demo.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ReviewService {

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ChatParticipantMapper chatParticipantMapper;

    // 리뷰 작성 (rating 자동 업데이트 포함)
    public void createReview(CreateReviewDto dto, int reviewerId) {
        // 1. 비즈니스 로직 검증
        validateReviewCreation(dto.getPostId(), reviewerId);

        // 2. 게시글 정보로 판매자 ID 조회
        Post post = postMapper.selectPostById(dto.getPostId());
        int reviewedId = post.getUserId();

        // 본인에게 리뷰 작성 방지
        if (reviewerId == reviewedId) {
            throw new IllegalStateException("본인에게는 리뷰를 작성할 수 없습니다.");
        }

        // 3. Review 엔티티 생성 및 저장
        Review review = new Review();
        review.setPostId(dto.getPostId());
        review.setReviewerId(reviewerId);
        review.setReviewedId(reviewedId);
        review.setPoint(dto.getPoint());
        review.setContent(dto.getContent());

        reviewMapper.insert(review);

        // 4. 판매자의 평균 평점 계산 및 업데이트
        updateUserRating(reviewedId);
    }

    // 사용자별 리뷰 목록 조회
    public List<ReviewListDto> getReviewsByUser(int reviewedId, int page, int size) {
        int offset = (page - 1) * size;
        return reviewMapper.selectByReviewedId(reviewedId, offset, size);
    }

    // 사용자 리뷰 통계 조회
    public UserReviewSummaryDto getUserReviewSummary(int userId) {
        return reviewMapper.selectUserReviewSummary(userId);
    }

    // 리뷰 작성 가능 여부 확인
    public boolean canWriteReview(int postId, int reviewerId) {
        Review existingReview = reviewMapper.selectByPostAndReviewer(postId, reviewerId);
        return existingReview == null;
    }

    // 사용자 평점 업데이트 (private 메서드)
    private void updateUserRating(int userId) {
        Double averageRating = reviewMapper.selectAverageRatingByUserId(userId);
        if (averageRating != null) {
            userMapper.updateUserRating(userId, averageRating);
        }
    }

    // 리뷰 생성시, 검증 로직
    private void validateReviewCreation(int postId, int reviewerId) {
        // 1. 중복 리뷰 확인
        Review existingReview = reviewMapper.selectByPostAndReviewer(postId, reviewerId);
        if (existingReview != null) {
            throw new IllegalStateException("이미 리뷰를 작성하셨습니다.");
        }

        // 2. 게시글 존재 확인
        Post post = postMapper.selectPostById(postId);
        if (post == null) {
            throw new IllegalStateException("존재하지 않는 게시글입니다.");
        }

        // 3. 상품 정보 및 거래 완료 상태 확인
        Product product = productMapper.selectByPostId(postId);
        if (product == null) {
            throw new IllegalStateException("상품 정보를 찾을 수 없습니다.");
        }

        if (product.getTradeStatus() != Product.TradeStatus.COMPLETED) {
            throw new IllegalStateException("거래가 완료된 상품에만 리뷰를 작성할 수 있습니다.");
        }

        // 4. 본인 상품에 리뷰 작성 방지
        if (post.getUserId() == reviewerId) {
            throw new IllegalStateException("본인이 판매한 상품에는 리뷰를 작성할 수 없습니다.");
        }

        // 5. 구매자 확인 (채팅방 참여 여부로 판단)
        boolean isParticipant = chatParticipantMapper.existsByPostIdAndUserId(postId, reviewerId);
        if (!isParticipant) {
            throw new IllegalStateException("해당 상품의 거래에 참여하지 않은 사용자는 리뷰를 작성할 수 없습니다.");
        }
    }
}