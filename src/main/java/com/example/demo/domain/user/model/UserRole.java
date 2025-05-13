package com.example.demo.domain.user.model;

import lombok.Data;

import java.util.Date;

@Data
public class UserRole {
	private int roleId;
	private int userId;
	private String roleName;
	private Date createdAt;
	private Date updatedAt;
}