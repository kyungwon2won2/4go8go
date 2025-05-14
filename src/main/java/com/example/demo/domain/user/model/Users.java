package com.example.demo.domain.user.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
public class Users {
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
	private Date createdAt;
	private Date updatedAt;

	// 권한 목록
	List<UserRole> roleList;
}