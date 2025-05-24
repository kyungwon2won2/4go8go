package com.example.demo.domain.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Users implements Serializable {
	private static final long serialVersionUID = 1L;

	private int userId;
	private String email;
	private String name;      // 추가
	private String phone;     // 추가
	private String password;
	private String nickname;
	private String address;
	private Date birthDate;
	private int points;
	private BigDecimal rating;
	private boolean receiveMail;
	private boolean emailVerified; // 이메일 인증 상태 필드 추가
	private Date createdAt;
	private Date updatedAt;
	private String socialType;    // GOOGLE, KAKAO, NAVER 등
	private String socialId;      // 소셜 서비스의 고유 ID
	private String oauth2AccessToken;
	private String status; // 유저의 활동상태 관리 "ACTIVE", "SUSPENDED", "DELETED" 등
	private Date deletedAt; // 탈퇴 시간 기록

	// 권한 목록
	List<UserRole> roleList;
}