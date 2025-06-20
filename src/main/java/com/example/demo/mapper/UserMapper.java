package com.example.demo.mapper;

import com.example.demo.domain.user.model.UserRole;
import com.example.demo.domain.user.model.Users;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Map;

@Mapper
public interface UserMapper {

    //로그인 사용자 인증
    public Users login(String username);

    //회원가입
    public int join(Users user) throws Exception;

    //회원 권한 등록
    public int insertAuth(UserRole userRole) throws Exception;

    // 모든 사용자 조회
    public List<Users> getAllUsers();

    // ID로 사용자 조회
    public Users getUserById(String userId);
    
    // Integer ID로 사용자 조회
    public Users findById(Integer userId);

    // 이메일로 사용자 조회
    public Users getUserByEmail(String email);
    
    // 이메일로 사용자 조회 (별칭)
    public Users findByEmail(String email);

   // 사용자 정보 수정
    public int updateUser(Users user);

    // 사용자 삭제
    public int deleteUser(String userId);

    List<Users> findUsersByBirthdayToday();
    
    // 페이징을 위한 생일 사용자 조회 (MyBatisPagingItemReader용)
    List<Users> findUsersByBirthdayTodayWithPaging();

    public boolean existsByEmail(String email);

    // 30일 지난 탈퇴 계정들 조회
    public List<Users> findExpiredDeletedAccounts();
    
    // === 완전 삭제를 위한 참조 데이터 삭제 메서드들 ===
    
    // 1. 채팅 메시지 삭제
    public int deleteChatMessagesByUserId(int userId);
    
    // 2. 채팅 참가자 삭제  
    public int deleteChatParticipantsByUserId(int userId);
    
    // 3. 채팅방 삭제 (해당 유저가 생성한)
    public int deleteChatRoomsByUserId(int userId);
    
    // 4. 댓글 삭제
    public int deleteCommentsByUserId(int userId);
    
    // 5. 알림 삭제
    public int deleteNotificationsByUserId(int userId);
    
    // 6. 읽음 상태 삭제
    public int deleteReadStatusByUserId(int userId);
    
    // 7. 사용자 권한 삭제
    public int deleteUserRolesByUserId(int userId);
    
    // 8. 최종 사용자 삭제
    public int permanentlyDeleteUser(int userId);
  
    // 9. 생일쿠폰 입력 시 유저 점수 추가
    int updatePoint(int userId, int discountAmount);

    // === 관리자 기능을 위한 추가 메서드들 ===
    
    // 사용자의 권한 목록 조회
    public List<UserRole> getUserRolesByUserId(@Param("userId") Integer userId);
    
    // 관리자 권한을 가진 사용자 목록 조회
    public List<Users> getAdminUsers();
    
    // 특정 권한 삭제
    public int deleteUserRole(@Param("userId") Integer userId, @Param("roleName") String roleName);

    // 사용자 평점 업데이트
    void updateUserRating(@Param("userId") int userId, @Param("rating") double rating);

    // === 통계 기능을 위한 쿼리들 ===
    
    // 전체 회원 수
    public int getTotalUserCount();
    
    // 상태별 회원 수
    public int getUserCountByStatus(@Param("status") String status);
    
    // 오늘 가입한 회원 수
    public int getTodayRegistrationCount();
    
    // 이번 주 가입한 회원 수
    public int getThisWeekRegistrationCount();
    
    // 이번 달 가입한 회원 수
    public int getThisMonthRegistrationCount();
    
    // 소셜 로그인 타입별 회원 수
    public int getUserCountBySocialType(@Param("socialType") String socialType);
    
    // 일반 가입 회원 수 (소셜 로그인 아닌)
    public int getNormalRegistrationCount();
    
    // 연령대별 회원 수 (10대, 20대, 30대 등)
    public List<Map<String, Object>> getUserCountByAgeGroup();
    
    // 평점 높은 상위 회원 조회
    public List<Users> getTopRatedUsers(@Param("limit") int limit);

}
