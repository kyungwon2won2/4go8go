package com.example.demo.mapper;

import com.example.demo.domain.review.dto.ReviewDetailDto;
import com.example.demo.domain.review.dto.ReviewListDto;
import com.example.demo.domain.review.dto.UserReviewSummaryDto;
import com.example.demo.domain.review.model.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReviewMapper {

    // 리뷰 작성
    void insert(Review review);

    // 리뷰 조회
    Review selectByPostAndReviewer(@Param("postId") int postId, @Param("reviewerId") int reviewerId);

    // 특정 사용자가 받은 리뷰 목록 + 페이징
    List<ReviewListDto> selectByReviewedId(@Param("reviewedId") int reviewedId,
                                           @Param("offset") int offset,
                                           @Param("limit") int limit);

    // 리뷰 상세 조회
    ReviewDetailDto selectDetailByPostAndReviewer(@Param("postId") int postId,
                                                  @Param("reviewerId") int reviewerId);

    // 사용자 리뷰 통계
    UserReviewSummaryDto selectUserReviewSummary(@Param("userId") int userId);

    // 특정 사용자 리뷰 개수
    int countByReviewedId(@Param("reviewedId") int reviewedId);

    // 사용자 평균 평점 계산 -> rating 업데이트를 위한 메서드
    Double selectAverageRatingByUserId(@Param("userId") int userId);
}