package com.example.demo.common.security.service;

import com.example.demo.common.email.EmailService;
import com.example.demo.domain.user.model.EmailVerification;
import com.example.demo.domain.user.model.UserRole;
import com.example.demo.domain.user.model.Users;
import com.example.demo.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public Users login(String username) {
        return userMapper.login(username);
    }

    @Override
    @Transactional
    public int join(Users user) throws Exception {
        // 비밀번호 암호화
        String password = user.getPassword();
        String encodedPassword = passwordEncoder.encode(password);
        user.setPassword(encodedPassword);
        
        // 이메일 인증이 필요하므로 기본값으로 인증되지 않음 상태로 설정
        user.setEmailVerified(false);

        // 회원 등록
        int result = userMapper.join(user);

        // 마지막으로 삽입된 사용자 ID 조회
        if(result > 0) {
            UserRole userRole = new UserRole();
            userRole.setUserId(user.getUserId());
            userRole.setRoleName("ROLE_USER");
            result += userMapper.insertAuth(userRole);
            
            // 이메일 인증 토큰 생성 및 이메일 발송
            String token = createEmailVerification(user.getEmail());
            
            // 이메일 발송
            emailService.sendVerificationEmail(user.getEmail(), user.getNickname());
        }
        return result;
    }

    @Override
    public int insertAuth(UserRole userRole) throws Exception {
        return userMapper.insertAuth(userRole);
    }

    @Override
    public List<Users> getAllUsers() {
        return userMapper.getAllUsers();
    }

    @Override
    public Users getUserById(String userId) {
        return userMapper.getUserById(userId);
    }
    
    @Override
    public Users getUserByEmail(String email) {
        return userMapper.getUserByEmail(email);
    }

    @Override
    public int updateUser(Users user) {
        // 비밀번호가 입력된 경우에만 암호화 처리
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encodedPassword);
        }

        return userMapper.updateUser(user);
    }

    @Override
    public int deleteUser(String userId) {
        return userMapper.deleteUser(userId);
    }
    
    @Override
    @Transactional
    public String createEmailVerification(String email) {
        // 이메일 인증 토큰 생성
        String token = null;
        try {
            Users user = userMapper.getUserByEmail(email);
            if (user != null) {
                // 인증 토큰 생성 및 발송
                token = emailService.sendVerificationEmail(email, user.getNickname());
                
                if (token != null) {
                    // 인증 토큰 저장
                    EmailVerification verification = new EmailVerification(email, token);
                    userMapper.saveEmailVerification(verification);
                }
            }
        } catch (Exception e) {
            log.error("이메일 인증 토큰 생성 실패: {}", email, e);
        }
        return token;
    }
    
    @Override
    public EmailVerification getEmailVerification(String token) {
        return userMapper.getEmailVerification(token);
    }
    
    @Override
    @Transactional
    public boolean verifyEmail(String token) {
        try {
            // 토큰으로 인증 정보 조회
            EmailVerification verification = userMapper.getEmailVerification(token);
            
            if (verification != null && !verification.isVerified() && !verification.isExpired()) {
                // 인증 상태 업데이트
                verification.setVerified(true);
                verification.setVerifiedAt(new Date());
                userMapper.updateEmailVerification(verification);
                
                // 사용자 이메일 인증 상태 업데이트
                userMapper.updateUserEmailVerified(verification.getEmail(), true);
                return true;
            }
        } catch (Exception e) {
            log.error("이메일 인증 처리 실패: {}", token, e);
        }
        return false;
    }
}