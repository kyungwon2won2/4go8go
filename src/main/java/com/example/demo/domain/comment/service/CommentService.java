package com.example.demo.domain.comment.service;

import com.example.demo.common.notification.model.Notification;
import com.example.demo.common.notification.service.NotificationService;
import com.example.demo.domain.comment.dto.CommentDTO;
import com.example.demo.domain.comment.model.Comment;
import com.example.demo.domain.post.dto.GeneralDetailDto;
import com.example.demo.domain.user.model.CustomerUser;
import com.example.demo.mapper.CommentMapper;
import com.example.demo.mapper.PostMapper;
import com.example.demo.mapper.UserMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentMapper commentMapper;
    private final PostMapper postMapper;
    private final NotificationService notificationService;
    private final UserMapper userMapper; // 사용자 닉네임 조회용

    @Transactional
    public Map<String, Object> createComment(int postId, String commentContent, CustomerUser loginUser) {
        if (loginUser == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        if (commentContent == null || commentContent.trim().isEmpty()) {
            throw new IllegalArgumentException("댓글 내용이 비어있습니다.");
        }

        // 1. 댓글 엔티티 생성
        Comment comment = new Comment(
                postId,
                loginUser.getUserId(),
                commentContent,
                new Date() // createdAt
        );

        // 2. DB에 삽입 (insertComment가 생성된 ID를 세팅해주어야 함)
        commentMapper.insertComment(comment);  // 이 시점에 comment.getCommentId() 값이 있어야 함

        // 3. 삽입된 댓글 ID로 조회
        CommentDTO newComment = commentMapper.selectCommentWithNickname(comment.getCommentId());

        // 4. 게시글 정보 조회하여 작성자 ID 가져오기
        GeneralDetailDto post = postMapper.selectPostByIdDto(postId);
        if (post != null) {
            int postWriterId = post.getUserId();

            // 5. 댓글 작성자와 게시글 작성자가 다른 경우에만 알림 생성
            if (postWriterId != loginUser.getUserId()) {
                // 6. 알림 생성
                String userNickname = loginUser.getNickname();

                Notification notification = Notification.builder()
                        .userId(postWriterId) // 수신자 ID (게시글 작성자)
                        .type("POST_COMMENT") // 알림 유형
                        .content(userNickname + "님이 회원님의 게시글에 댓글을 남겼습니다: " +
                                (commentContent.length() > 20 ? commentContent.substring(0, 20) + "..." : commentContent))
                        .url("/post/" + postId) // 클릭 시 이동할 URL (게시글 상세 페이지)
                        .referenceId((long) postId) // 참조 ID (게시글 ID)
                        .isRead(false) // 읽음 여부
                        .createdAt(new Date()) // 생성 시간
                        .build();

                // 7. 알림 서비스를 통해 알림 저장 및 전송
                notificationService.createNotification(notification);
            }
        }

        // 5. 응답 구성
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("comment", newComment);
        return response;
    }



    @Transactional
    public Map<String, Object> updateComment(int commentId, String commentContent, int userId){
        if (commentContent == null || commentContent.trim().isEmpty()) {
            throw new IllegalArgumentException("댓글 내용이 비어있습니다.");
        }
        Comment comment = commentMapper.selectComment(commentId);
        if (comment.getUserId() == userId) {
            comment.setContent(commentContent);
            comment.setUpdatedAt(new Date());
            commentMapper.updateComment(comment);
        } else {
            throw new IllegalStateException("댓글 수정 권한이 없습니다.");
        }
        CommentDTO updatedComment = commentMapper.selectCommentWithNickname(commentId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("comment", updatedComment);
        return response;
    }
    
    @Transactional
    public Map<String, Object> deleteComment(int commentId, int userId) {
        Comment comment = commentMapper.selectComment(commentId);
        if (comment.getUserId() == userId) {
            commentMapper.deleteComment(commentId);
        } else {
            throw new IllegalStateException("댓글 삭제 권한이 없습니다.");
        }
        // 삭제 후 남은 댓글 목록과 전체 개수 반환
        List<CommentDTO> comments = commentMapper.selectCommentsByPostWithNickname(comment.getPostId(), 0, Integer.MAX_VALUE);
        int totalCount = commentMapper.selectCommentCountByPostId(comment.getPostId());
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("comments", comments);
        response.put("totalCount", totalCount);
        return response;
    }

    // 댓글 1개 상세 조회 (닉네임 포함)
    public CommentDTO getCommentWithNickname(int commentId) {
        return commentMapper.selectCommentWithNickname(commentId);
    }
    
    // 게시글에 달린 댓글 목록 조회 (닉네임 포함, 페이징)
    public List<CommentDTO> getCommentsByPostWithNickname(int postId, int page, int size) {
        int offset = (page - 1) * size;
        return commentMapper.selectCommentsByPostWithNickname(postId, offset, size);
    }
    // (기존) 전체 목록 조회는 deprecated 또는 유지
    @Deprecated
    public List<CommentDTO> getCommentsByPostWithNickname(int postId) {
        return commentMapper.selectCommentsByPostWithNickname(postId, 0, Integer.MAX_VALUE);
    }

    // 게시글에 달린 댓글 목록(페이징) + 전체 개수 반환
    public Map<String, Object> getCommentsWithTotalByPostWithNickname(int postId, int page, int size) {
        int offset = (page - 1) * size;
        List<CommentDTO> comments = commentMapper.selectCommentsByPostWithNickname(postId, offset, size);
        int totalCount = commentMapper.selectCommentCountByPostId(postId);
        Map<String, Object> result = new HashMap<>();
        result.put("comments", comments);
        result.put("totalCount", totalCount);
        return result;
    }
}
