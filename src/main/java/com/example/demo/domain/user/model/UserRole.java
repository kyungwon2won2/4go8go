package com.example.demo.domain.user.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class UserRole implements Serializable {
	private static final long serialVersionUID = 1L;

	private int roleId;
	private int userId;
	private String roleName;
	private Date createdAt;
	private Date updatedAt;
}