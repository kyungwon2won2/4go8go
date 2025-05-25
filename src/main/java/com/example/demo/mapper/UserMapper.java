package com.example.demo.mapper;

import com.example.demo.domain.user.model.UserRole;
import com.example.demo.domain.user.model.Users;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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

    // === 관리자 기능을 위한 추가 메서드들 ===
    
    // 사용자의 권한 목록 조회
    public List<UserRole> getUserRolesByUserId(@Param("userId") Integer userId);
    
    // 관리자 권한을 가진 사용자 목록 조회
    public List<Users> getAdminUsers();
    
    // 특정 권한 삭제
    public int deleteUserRole(@Param("userId") Integer userId, @Param("roleName") String roleName);
}
