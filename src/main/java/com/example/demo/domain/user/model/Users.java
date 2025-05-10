package com.example.demo.domain.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/*
CREATE TABLE `user` (
  `USER_NO` int NOT NULL AUTO_INCREMENT,
  `USER_ID` varchar(100) NOT NULL,
  `USER_PW` varchar(200) NOT NULL,
  `NAME` varchar(100) NOT NULL,
  `EMAIL` varchar(200) DEFAULT NULL,
  `REG_DATE` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `UPD_DATE` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `ENABLED` int DEFAULT 1,
  PRIMARY KEY (`USER_NO`)
) COMMENT='회원';
 */
@Data
@NoArgsConstructor
public class Users {
	private int userNo;
	private String userId;
	private String userPw;
	private String name;
	private String email;
	private Date regDate;
	private Date updDate;
	private int enabled;
	
	//권한 목록
	List<UserAuth> authList; 
	
}
